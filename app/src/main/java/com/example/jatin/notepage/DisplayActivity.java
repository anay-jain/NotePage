package com.example.jatin.notepage;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DisplayActivity extends AppCompatActivity {

    private List<Note> messages = new ArrayList<>();
    private RecyclerView recyclerView;
    private DisplayListAdapter displayListAdapter;
    private Menu menu;
    private Database db;
    private int title_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //getting the note_id in the variable
        title_id = getIntent().getIntExtra("title_id", -1);

        //if there is no such id move back to previous activity
        if (title_id == -1) this.finish();

        //populating the messages according to note_id
        recyclerView = (RecyclerView)findViewById(R.id.list_recycler);
        displayListAdapter = new DisplayListAdapter(messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(displayListAdapter);

        //adding data to list
        db = new Database(DisplayActivity.this);
        messages = db.getNotes(title_id);

        //updating title bar
        getSupportActionBar().setTitle(db.getTitle(title_id).getTitle());

        //updating the list
        displayListAdapter = new DisplayListAdapter(messages);
        recyclerView.setAdapter(displayListAdapter);
        //displayListAdapter.notifyDataSetChanged();

        //scroll to the newest note
        scrollToBottom();


        //edit text reference of message box
        final EditText message = (EditText) findViewById(R.id.message);
        ImageView button = (ImageView) findViewById(R.id.send_button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String data = message.getText().toString();
                data.trim();
                if (!data.equals("") && data.length()<200){
                    addNote(new Note(data), title_id, db);
                    message.setText("");
                }
            }
        });

    }

    public void updateList(){
        List<Note> messages = db.getNotes(title_id);
        updateList(messages);
    }
    public void updateList(List<Note> messages){
        displayListAdapter= new DisplayListAdapter(messages);
        recyclerView.setAdapter(displayListAdapter);
    }

    @Override
    public void onBackPressed() {
        if (displayListAdapter.isSelectionMode()){
            displayListAdapter.onBackPress();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_display_activity, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.delete:
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete selected messages")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                List<Note> notes = displayListAdapter.getSelected();
                                int size = notes.size();
                                for (int j=0; j<size; j++){
                                    db.delNote(notes.get(j).getId());
                                }
                                displayListAdapter.setSelectionMode(false);
                                updateList();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                break;
            case R.id.all:
                item.setVisible(false);
                menu.findItem(R.id.incomplete).setVisible(true);
                updateList();
                break;
            case R.id.incomplete:
                item.setVisible(false);
                menu.findItem(R.id.all).setVisible(true);
                updateList(db.getUnCompletedNotes(title_id));
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return true;
    }

    private void addNote(Note note, int title_id, Database db){
        try{
            db.addNote(note.getMessage(), title_id);
            messages.add(note);

        } catch (Database.DatabaseException e){
            Log.e("jaitn", "not added");
        }

        updateList();
        scrollToBottom();
    }

    private void scrollToBottom(){
        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount());

    }

    public class DisplayListAdapter extends RecyclerView.Adapter<DisplayListAdapter.ViewHolder> {

        List<Note> messages = new ArrayList<>();

        private boolean selectionMode = false;
        private List<Note> selected = new ArrayList<>();
        private List<RelativeLayout> cards = new ArrayList<>();

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView message;
            public RelativeLayout cardLayout;
            public CheckBox checkBox;

            public ViewHolder(View itemView) {
                super(itemView);
                message = (TextView)itemView.findViewById(R.id.message);
                cardLayout = (RelativeLayout) itemView.findViewById(R.id.card_layout);
                cards.add(cardLayout);
                checkBox = (CheckBox) itemView.findViewById(R.id.complete);
            }
        }

        public DisplayListAdapter(List<Note> messages){
            this.messages = messages;
        }

        public void setSelectionMode(boolean mode){
            selectionMode = mode;
            MenuItem item = DisplayActivity.this.menu.findItem(R.id.delete);
            if (mode){
                item.setVisible(true);
            } else {
                item.setVisible(false);
                clearSelection();
            }
        }

        public void onBackPress(){
            int size = cards.size();
            for (int i=0; i<size; i++){
                cards.get(i).setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            setSelectionMode(false);
        }

        public void clearSelection(){
            selected.clear();
        }

        public List<Note> getSelected(){
            return selected;
        }

        public boolean isSelectionMode(){
            return selectionMode;
        }

        @Override
        public DisplayListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_list_layout, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final DisplayListAdapter.ViewHolder holder, final int position) {
            holder.message.setText(messages.get(position).getMessage());
            holder.cardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectionMode){
                        if (selected.contains(messages.get(position))){
                            Log.e("jaitn","contains this note");
                            selected.remove(messages.get(position));
                            holder.cardLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            if (selected.size() == 0) setSelectionMode(false);
                        } else {
                            selected.add(messages.get(position));
                            holder.cardLayout.setBackgroundColor(Color.parseColor("#FFC8F482"));
                        }
                    } else {
                        return;
                    }
                }
            });
            if (messages.get(position).getCompleted()){
                holder.checkBox.setChecked(true);
            }
            holder.cardLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!selectionMode){
                        setSelectionMode(true);
                        selected.add(messages.get(position));
                        holder.cardLayout.setBackgroundColor(Color.parseColor("#FFC8F482"));
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    db.setNoteComplete(messages.get(position).getId(), b);
                }
            });
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }
}
