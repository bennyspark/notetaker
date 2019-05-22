package com.app.ben.notetaker;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener {
    private TextView mAddNoteText;
    private ImageView mAddNoteImage;
    private static final String KEY_NAME = "viewState";
    private String mFileName;
    private ListView mListNotes;
    private GridView mGridNotes;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    NoteListAdapter na;
    NoteGridAdapter nb;
    SharedPreferences sharedPreferences, theme_preferences;
    Constant constant;
    int app_theme, app_color, theme_color;
    SharedPreferences.Editor editor;
    private boolean mViewIsChanged;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve value of theme state from Shared Preferences
        theme_preferences = getSharedPreferences("color", 0);
        theme_preferences = getSharedPreferences("theme", 0);
        app_theme = theme_preferences.getInt("theme", 0);
        app_color = theme_preferences.getInt("color", 0);
        theme_color = theme_preferences.getInt("theme", 0);
        theme_color = app_color;
        constant.color = app_color;
        if (theme_color == 0){
            setTheme(Constant.theme);
        } else if (app_theme == 0){
            setTheme(Constant.theme);
        } else {
            setTheme(app_theme);
        }
        setContentView(R.layout.activity_main);
        // set the layouts for list/grid
        mListNotes = (ListView) findViewById(R.id.main_listview);
        mGridNotes = (GridView) findViewById(R.id.main_gridview);

        if (mViewIsChanged){
            mListNotes.setVisibility(View.VISIBLE);
            mGridNotes.setVisibility(View.GONE);}
        else {
            mListNotes.setVisibility(View.GONE);
            mGridNotes.setVisibility(View.VISIBLE);
        }
        setupToolbar();
        // Retrieve value of view toggle from Shared Preferences.
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        mViewIsChanged = sharedPreferences.getBoolean(KEY_NAME, false);
        if (!mViewIsChanged){
            mListNotes.setVisibility(View.VISIBLE);
            mGridNotes.setVisibility(View.GONE);}
        else {
            mListNotes.setVisibility(View.GONE);
            mGridNotes.setVisibility(View.VISIBLE);
        }
mAddNoteText = (TextView) findViewById(R.id.add_note_text);
        mAddNoteImage = (ImageView) findViewById(R.id.add_note_image);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mListNotes.setTextFilterEnabled(true);
        registerForContextMenu(mListNotes);
        registerForContextMenu(mGridNotes);
        // setting checked feature to the navigation item
        navigationView.setCheckedItem(R.id.nav_notes);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                toolbar.setTranslationX(slideOffset * drawerView.getWidth());
                mGridNotes.setTranslationX(slideOffset * drawerView.getWidth());
                mListNotes.setTranslationX(slideOffset * drawerView.getWidth());
                mAddNoteText.setTranslationX(slideOffset * drawerView.getWidth());
                mAddNoteImage.setTranslationX(slideOffset * drawerView.getWidth());
                drawerLayout.bringChildToFront(drawerView);
                drawerLayout.requestLayout();
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    // handling context menu clicks
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Note mLoadedNote = (Note) mListNotes.getAdapter().getItem(info.position);
        final Note mLoadedNoteGrid = (Note) mGridNotes.getAdapter().getItem(info.position);
        mFileName = ((Note) mListNotes.getAdapter().getItem(info.position)).getDateTime() + Utilities.FILE_EXTENSION;
        mLoadedNote.getTitle();
        mLoadedNoteGrid.getTitle();
        switch (item.getItemId()) {
            case R.id.edit:
                final String fileName = ((Note) mListNotes.getAdapter().getItem(info.position)).getDateTime() + Utilities.FILE_EXTENSION;
                final String GridfileName = ((Note) mGridNotes.getAdapter().getItem(info.position)).getDateTime() + Utilities.FILE_EXTENSION;
                Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
                intent.putExtra(Utilities.EXTRAS_NOTE_FILENAME, fileName);
                intent.putExtra(Utilities.EXTRAS_NOTE_FILENAME, GridfileName);
                startActivity(intent);
                break;
            case R.id.delete:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                        .setTitle("delete " + mLoadedNote.getTitle())
                        .setMessage("are you sure?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Utilities.deleteFile(getApplicationContext(), mFileName)) {
                                    ArrayAdapter<Note> arrayAdapter = (ArrayAdapter<Note>) mListNotes.getAdapter();
                                    ArrayAdapter<Note> arrayAdapterGrid = (ArrayAdapter<Note>) mGridNotes.getAdapter();
                                    arrayAdapter.remove(arrayAdapter.getItem(info.position));
                                    arrayAdapter.notifyDataSetChanged();
                                    arrayAdapterGrid.notifyDataSetChanged();
                                    Toast.makeText(MainActivity.this, mLoadedNote.getTitle() + " is deleted", Toast.LENGTH_SHORT).show();
                                    if (arrayAdapter.getCount() == 0 || arrayAdapterGrid.getCount() == 0) {
                                        mAddNoteText.setVisibility(View.VISIBLE);
                                        mAddNoteImage.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "can not delete the note '" + mLoadedNote.getTitle() + "'", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("NO", null); //do nothing on clicking NO button :P
                alertDialog.show();
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Constant.color);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    private void setIconInMenu(Menu menu, int menuItemId, int labelId, int iconId) {
        MenuItem item = menu.findItem(menuItemId);
        SpannableStringBuilder builder = new SpannableStringBuilder("    " + getResources().getString(labelId));
        builder.setSpan(new ImageSpan(this, iconId), 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        item.setTitle(builder);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(this);
        setIconInMenu(menu, R.id.tools, R.string.theme_settings, R.drawable.ic_settings_black_24dp);
        setIconInMenu(menu, R.id.changeView, R.string.view, R.drawable.ic_cached_black_24dp);
        setIconInMenu(menu, R.id.search, R.string.search, R.drawable.ic_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View customTitleView = inflater.inflate(R.layout.dialog_menu, null);
        LinearLayout mListViewSelect = (LinearLayout) customTitleView.findViewById(R.id.list_select);
        LinearLayout mGridViewSelect = (LinearLayout) customTitleView.findViewById(R.id.grid_select);
        switch (item.getItemId()) {
            case R.id.addItem:
              //  start NoteActivity
                startActivity(new Intent(this, NoteActivity.class));
                break;
            case R.id.changeView:
                final AlertDialog alertbox = new AlertDialog.Builder(this).create();
                alertbox.setCancelable(true);
                alertbox.setView(customTitleView);
                alertbox.show();
                mListViewSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewIsChanged = false;
                        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
                        editor = sharedPreferences.edit();
                        editor.putBoolean(KEY_NAME, mViewIsChanged);
                        editor.apply();
                        mListNotes.setVisibility(View.VISIBLE);
                        mGridNotes.setVisibility(View.GONE);
                        alertbox.dismiss();
                    }
                });
        mGridViewSelect.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        mViewIsChanged = true;
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean(KEY_NAME, mViewIsChanged);
        editor.apply();
        mListNotes.setVisibility(View.GONE);
        mGridNotes.setVisibility(View.VISIBLE);
        alertbox.dismiss();
    }
});
                break;
            case R.id.tools:
                Intent intent = new Intent(this, ThemeActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //load saved notes into the listview
        //first, reset the listview
        mListNotes.setAdapter(null);
        mGridNotes.setAdapter(null);
        ArrayList<Note> notes = Utilities.getAllSavedNotes(getApplicationContext());
        //sort notes from new to old
        Collections.sort(notes, new Comparator<Note>() {
            @Override
            public int compare(Note lhs, Note rhs) {
                if(lhs.getDateTime() > rhs.getDateTime()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        if(notes != null && notes.size() > 0) { //check if we have any notes!
           na = new NoteListAdapter(this, R.layout.list_component, notes);
            nb = new NoteGridAdapter(this, R.layout.grid_component, notes);
            mListNotes.setAdapter(na);
            mGridNotes.setAdapter(nb);
            //remove the add notes reminder
            mAddNoteText.setVisibility(View.INVISIBLE);
            mAddNoteImage.setVisibility(View.INVISIBLE);
            //set click listener for items in the list, by clicking each item the note should be loaded into NoteActivity
            mListNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //run the NoteActivity in view/edit mode


                    String fileName = ((Note) mListNotes.getItemAtPosition(position)).getDateTime()
                            + Utilities.FILE_EXTENSION;
                    Intent viewNoteIntent = new Intent(getApplicationContext(), NoteActivity.class);
                    viewNoteIntent.putExtra(Utilities.EXTRAS_NOTE_FILENAME, fileName);
                    startActivity(viewNoteIntent);

                }
            });
            mGridNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //run the NoteActivity in view/edit mode
                    String fileNameGrid = ((Note) mGridNotes.getItemAtPosition(position)).getDateTime()
                            + Utilities.FILE_EXTENSION;
                    Intent viewNoteIntent = new Intent(getApplicationContext(), NoteActivity.class);
                    viewNoteIntent.putExtra(Utilities.EXTRAS_NOTE_FILENAME, fileNameGrid);
                    startActivity(viewNoteIntent);

                }
            });
        } else { //remind user that we have no notes!
            mAddNoteText.setVisibility(View.VISIBLE);
            mAddNoteImage.setVisibility(View.VISIBLE);
        }



    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //Checking if the item is in checked state or not, if not make it in checked state
        if (item.isChecked()) item.setChecked(false);
        else item.setChecked(true);
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_notes) {
            Intent noteIntent = new Intent(this, MainActivity.class);
            startActivity(noteIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        } else if(id == R.id.nav_backup) {
            Intent backupIntent = new Intent(this, BackNotesActivity.class);
            startActivity(backupIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        } else if(id == R.id.nav_theme) {
            Intent themeIntent = new Intent(this, ThemeActivity.class);
            startActivity(themeIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        } else if (id == R.id.nav_share) {

            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "NoteTaker");
                String sAux = "\nLet me recommend you this application\n\n";
                sAux = sAux + "https://play.google.com/store/apps/details?id=com.app.ben.notetaker \n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "choose one"));
            } catch(Exception e) {
                e.toString();
            }

        } else if (id == R.id.nav_send) {
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setType("text/plain");
            email.setData(Uri.parse("mailto:benkeys@musicianfocus.com"));
            email.putExtra(Intent.EXTRA_SUBJECT, "Enter Subject Here");
            email.putExtra(Intent.EXTRA_TEXT, "Type your message");
            try {
                startActivity(email);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "No email apps installed", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Toast.makeText(this, "Unexpected Error", Toast.LENGTH_SHORT).show();
            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        na.getFilter().filter(query);
        nb.getFilter().filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        na.getFilter().filter(query);
        nb.getFilter().filter(query);
        return false;
    }
}
