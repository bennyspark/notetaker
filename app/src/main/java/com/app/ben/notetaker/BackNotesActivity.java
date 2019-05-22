package com.app.ben.notetaker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;

public class BackNotesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ScrollView scroll;
    private Button backup_button;
    private CoordinatorLayout coordinatorLayout;
    int app_theme, app_color, theme_color;
    SharedPreferences.Editor editor;
    SharedPreferences theme_preferences;
    Constant constant;
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
        setContentView(R.layout.activity_back_notes);
        setupToolbar();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        backup_button = (Button) findViewById(R.id.backup_button);
        backup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDummyProgressDialog();
            }
        });
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_backup);
        scroll = (ScrollView) findViewById(R.id.scrollview);
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
                scroll.setTranslationX(slideOffset * drawerView.getWidth());
                drawerLayout.bringChildToFront(drawerView);
                drawerLayout.requestLayout();
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void myDummyProgressDialog() {
      ArrayList<Note>  note= Utilities.getAllSavedNotes(getApplicationContext());
        if (note != null && note.size() > 0) {
            final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Backup in progress...", true);
            progressDialog.show();
            // performing logic away from main thread
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showSnackbar();
                    progressDialog.dismiss();
                }

            }, 10000);
        } else {
            Toast.makeText(this, "No notes found!\nBackup cant be performed", Toast.LENGTH_LONG).show();
        }
    }

    private void showSnackbar() {
      final Snackbar snackBar = Snackbar.make(coordinatorLayout, "Backup successfully completed", Snackbar.LENGTH_INDEFINITE);
        snackBar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        });
        snackBar.show();
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_theme);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Constant.color);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
