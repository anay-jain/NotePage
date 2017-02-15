package com.example.jatin.notepage;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Title> titles = new ArrayList<>();
    private RecyclerView recyclerView;
    private MainListAdapter mainListAdapter;
    private Database db;
    private Menu menu;
    private SearchManager searchmanager ;
    private SearchView searchview;
    private MenuItem searchItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //content
        setContentView(R.layout.activity_main);

//        Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
//        intent.putExtra("note_id", 2);
//        startActivity(intent);


        //setting up recycler view
        recyclerView = (RecyclerView)findViewById(R.id.list_recycler);

        mainListAdapter = new MainListAdapter(titles);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mainListAdapter);

        //populating notes list
        db = new Database(this);

        //updating list
//        mainListAdapter.notifyDataSetChanged();

        FloatingActionButton addButton = (FloatingActionButton)findViewById(R.id.add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(MainActivity.this, R.style.DialogTheme);
                dialog.setContentView(R.layout.add_popup_layout);
                Button add = (Button)dialog.findViewById(R.id.add);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String title = ((EditText) dialog.findViewById(R.id.title)).getText().toString();
                        title.trim();
                        if (!title.equals("") && title.length() < 200){
                            db.addTitle(title);
                            updateList();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid Title", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.show();
            }
        });


    }

    public void updateList(){
        titles = db.getTitles();
//        Log.e("jatin", titles.get(0).getTitle());

        mainListAdapter = new MainListAdapter(titles);

        recyclerView.setAdapter(mainListAdapter);
    }



    @Override
    protected void onStart() {
        super.onStart();
        updateList();
        if (searchItem != null)
            searchItem.collapseActionView();
    }
    @Override
    public boolean onSearchRequested(){
        //pauseSomeStuff();
        return super.onSearchRequested();
    }



    @Override
    public void onBackPressed() {
        if (mainListAdapter.isSelectionMode()){
            mainListAdapter.onBackPress();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        this.menu = menu;
        // set the search view and the searchable menu config
         searchmanager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         searchview =(SearchView) menu.findItem(R.id.search).getActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete:

                new AlertDialog.Builder(this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete selected notes?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                List<Title> selected = mainListAdapter.getSelectedTitles();
                                int size = selected.size();
                                for (int j=0; j<size; j++){
                                    db.delTitle(selected.get(j).getId());
                                }
                                mainListAdapter.clearSelection();
                                mainListAdapter.setSelectionMode(false);
                                updateList();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                break;
            case R.id.add:
                final Dialog dialog = new Dialog(this, R.style.DialogTheme);
                dialog.setContentView(R.layout.add_popup_layout);
                Button add = (Button) dialog.findViewById(R.id.add);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String title = ((EditText) dialog.findViewById(R.id.title)).getText().toString();
                        title.trim();
                        if (title != "" && title.length() < 200) {
                            db.addTitle(title);
                            updateList();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid Title", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.show();
                break;
            case R.id.settings:
                Intent settingdisplay = new Intent(MainActivity.this , Settings.class);
                startActivity(settingdisplay);
                break;
            case R.id.edit_title:
                final Dialog edit_dialog = new Dialog(this , R.style.DialogTheme);
                    edit_dialog.setContentView(R.layout.edit_popup_layout);
                    Button edit_button = (Button) edit_dialog.findViewById(R.id.edit_button);
                    edit_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String title = ((EditText) edit_dialog.findViewById(R.id.edit_title)).getText().toString();
                            title.trim();
                            int titleid = mainListAdapter.getSelectedTitles().get(0).getId();
                            if(title !="" && title.length() < 20){
                              db.editTitle(titleid ,title);
                                updateList();
                                edit_dialog.dismiss();
                            }
                            else
                                Toast.makeText(MainActivity.this , "Invalid title" , Toast.LENGTH_SHORT).show();

                        }
                    });
                    edit_dialog.show();

                        break;

            case R.id.search:
                // assumes the current activity is the searchable activity
                searchview.setSearchableInfo(searchmanager.getSearchableInfo(getComponentName()));
                searchview.setIconifiedByDefault(false);
                searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {

                        titles = db.getTitles(newText);
                        mainListAdapter = new MainListAdapter(titles);
                        recyclerView.setAdapter(mainListAdapter);

                        return true;
                    }
                });
                break;

        }
        return true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        onStart();
    }

    //Adapter for the list
    public class MainListAdapter extends RecyclerView.Adapter<MainListAdapter.ViewHolder> {

        private List<Title> titles;

        private boolean selectionMode = false;
        private List<Title> selected= new ArrayList<>();
        private List<LinearLayout> layouts = new ArrayList<>();

        public class ViewHolder extends RecyclerView.ViewHolder {

            //getting the Views from the layout file
            public TextView title, data;
            public LinearLayout list_item;

            public ViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.title);
                data = (TextView) itemView.findViewById(R.id.data);
                list_item = (LinearLayout) itemView.findViewById(R.id.list_item);
            }
        }

        public boolean isSelectionMode() {
            return selectionMode;
        }

        public List<Title> getSelectedTitles() {
            return selected;
        }

        public void clearSelection() {
            selected.clear();
        }

        public void setSelectionMode(boolean mode) {
            selectionMode = mode;
            MenuItem delete = MainActivity.this.menu.findItem(R.id.delete);
            MenuItem edit = MainActivity.this.menu.findItem(R.id.edit_title);
            if (mode) {
                edit.setVisible(true);
                delete.setVisible(true);
            } else {
                delete.setVisible(false);
                edit.setVisible(false);
            }
        }

        public void onBackPress(){
            //clear all the selection when back is pressed
            clearSelection();
            int size = layouts.size();
            for (int i=0; i<size; i++){
                layouts.get(i).setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            setSelectionMode(false);
        }

        //constructor
        public MainListAdapter(List<Title> titles){
            this.titles = titles;
        }

        // Implemented methods
        @Override
        public MainListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_list_layout, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MainListAdapter.ViewHolder holder, int position) {
            final Title title = titles.get(position);
            holder.title.setText(title.getTitle());
            holder.data.setText(title.getLatestMessage());
            layouts.add(holder.list_item);
            holder.list_item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (selected.contains(title)){
                        return false;
                    } else {
                        setSelectionMode(true);
                        holder.list_item.setBackgroundColor(Color.parseColor("#FFC8F482"));
                        selected.add(title);
                        return true;
                    }
                }
            });
            holder.list_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectionMode){
                        if (selected.contains(title)){
                            selected.remove(title);
                            holder.list_item.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            if(selected.size() == 0) setSelectionMode(false);
                        } else {
                            holder.list_item.setBackgroundColor(Color.parseColor("#FFC8F482"));
                            selected.add(title);
                        }
                    } else {
                        Intent intent = new Intent (MainActivity.this, DisplayActivity.class);
                        intent.putExtra("title_id", title.getId());
                        startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return titles.size();
        }
    }

}
