package com.app.ben.notetaker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class NoteActivity extends AppCompatActivity {
    private EditText mEtTitle;
    private EditText mEtContent;
    private long mNoteCreationTime;
    private String mFileName;
    private boolean mIsUpdating; //handling user opening the activity in edit mode
    private boolean mIsViewing; // handling user viewing a note
    private boolean mIsNewNote; // handling user creating new note
    private Note mLoadedNote = null;
    SharedPreferences theme_preferences;
    int app_color, theme_color, app_theme;
    Constant constant;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve value of theme state from Shared Preferences
        theme_preferences = getSharedPreferences("color", 0);
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
        setContentView(R.layout.activity_note);
        setupToolbar();
        mEtTitle = (EditText) findViewById(R.id.note_et_title);
        mEtContent = (EditText) findViewById(R.id.note_et_content);
        // detecting double tap on activity
        final GestureDetector gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                editNote();
                if (mEtContent != null) {
                    mIsUpdating = true;
                }
                return true;
            }
        });

        mEtContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        //check if view/edit note bundle is set, otherwise user wants to create new note
        mFileName = getIntent().getStringExtra(Utilities.EXTRAS_NOTE_FILENAME);
        if(mFileName != null && !mFileName.isEmpty() && mFileName.endsWith(Utilities.FILE_EXTENSION)) {
            mLoadedNote = Utilities.getNoteByFileName(getApplicationContext(), mFileName);
            if (mLoadedNote != null) {
                //update the widgets from the loaded note
                mEtTitle.setText(mLoadedNote.getTitle());
                mEtContent.setText(mLoadedNote.getContent());
                mNoteCreationTime = mLoadedNote.getDateTime();
                mEtTitle.setVisibility(View.INVISIBLE);
                mIsViewing = true;
            }
        } else { //user wants to create a new note
            mEtContent.setFocusableInTouchMode(true);
            mEtContent.setCursorVisible(true);
            mNoteCreationTime = System.currentTimeMillis();
            mIsNewNote = true;
            mIsViewing = false;
        }
    }

    private void editNote() {
    mIsUpdating = true;
    invalidateOptionsMenu();  // refresh menu items so as to make save icon appear
    mEtTitle.setVisibility(View.VISIBLE);
    mEtContent.setFocusableInTouchMode(true);
    mEtContent.setCursorVisible(true);
    }
    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(Constant.color);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
// implementing logic to modify the menu content dynamically
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuEditItem = menu.findItem(R.id.preview_edit);
        MenuItem menuSaveItem = menu.findItem(R.id.action_update);
        if (mIsUpdating && mEtContent.getText().length() > 0) {
           menuEditItem.setVisible(false);
            menuSaveItem.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }
    // using Spannable String to make icons appear in overflow menu
    private void setIconInMenu(Menu menu, int menuItemId, int labelId, int iconId) {
        MenuItem item = menu.findItem(menuItemId);
        SpannableStringBuilder builder = new SpannableStringBuilder("    " + getResources().getString(labelId));
        builder.setSpan(new ImageSpan(this, iconId), 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        item.setTitle(builder);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //load menu based on the state we are in (new, view/update/delete)
        if(mIsNewNote) { //user is updating a note
            getMenuInflater().inflate(R.menu.menu_note_add, menu);
        } else if (mIsViewing) {
            getMenuInflater().inflate(R.menu.menu_activity_note_preview, menu);
            setIconInMenu(menu, R.id.send, R.string.send, R.drawable.ic_send_black_24dp);
            setIconInMenu(menu, R.id.action_delete, R.string.delete, R.drawable.ic_delete_black_24dp);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_save_note: //save the note
                validateAndSaveNote();
                break;

            case R.id.action_delete:
                actionDelete();
                break;

            case R.id.action_cancel: //cancel the note
                actionCancel();
                break;
            case R.id.preview_edit:
                editNote();
                invalidateOptionsMenu(); // refresh menu items so as to make the save menu icon appear
                break;
            case R.id.action_update: // update note
                validateAndUpdateNote();
                break;
            case R.id.send:
                String note = mEtContent.getText().toString();
                Intent sendNote = new Intent(android.content.Intent.ACTION_SEND);
                sendNote.setType("text/plain");
                sendNote.putExtra(Intent.EXTRA_TEXT, note);
                startActivity(Intent.createChooser(sendNote, "send note"));
        }


        return super.onOptionsItemSelected(item);
    }

    private void validateAndUpdateNote() {
        //get the content of widgets to make a note object
        String title = mEtTitle.getText().toString();
        String content = mEtContent.getText().toString();

        //see if user has entered anything :D lol
        if(title.isEmpty()) { //title
            Toast.makeText(NoteActivity.this, "please enter a title!"
                    , Toast.LENGTH_SHORT).show();
            return;
        }

        if(content.isEmpty()) { //content
            Toast.makeText(NoteActivity.this, "please enter a content for your note!"
                    , Toast.LENGTH_SHORT).show();
            return;
        }

        //set the creation time, if new note, now, otherwise the loaded note's creation time
        if(mLoadedNote != null) {
            mNoteCreationTime = mLoadedNote.getDateTime();
        } else {
            mNoteCreationTime = System.currentTimeMillis();
        }

        //finally save the note!
        if(Utilities.saveNote(this, new Note(mNoteCreationTime, title, content))) { //success!
            //tell user the note was saved!
            Toast.makeText(this, "your note has been updated", Toast.LENGTH_SHORT).show();
        } else { //failed to save the note! but this should not really happen :P :D :|
            Toast.makeText(this, "can not save the note. make sure you have enough space " +
                    "on your device", Toast.LENGTH_SHORT).show();
        }

        finish(); //exit the activity, should return us to MainActivity

    }

    @Override
    public void onBackPressed() {
       actionCancel();
    }

    private void validateAndSaveNote() {
        //get the content of widgets to make a note object
        String title = mEtTitle.getText().toString();
        String content = mEtContent.getText().toString();

        //see if user has entered anything :D lol
        if(title.isEmpty()) { //title
            Toast.makeText(NoteActivity.this, "please enter a title!"
                    , Toast.LENGTH_SHORT).show();
            return;
        }

        if(content.isEmpty()) { //content
            Toast.makeText(NoteActivity.this, "please enter a content for your note!"
                    , Toast.LENGTH_SHORT).show();
            return;
        }

        //set the creation time, if new note, now, otherwise the loaded note's creation time
        if(mLoadedNote != null) {
            mNoteCreationTime = mLoadedNote.getDateTime();
        } else {
            mNoteCreationTime = System.currentTimeMillis();
        }

        //finally save the note!
        if(Utilities.saveNote(this, new Note(mNoteCreationTime, title, content))) { //success!
            //tell user the note was saved!
            Toast.makeText(this, "your note has been saved", Toast.LENGTH_SHORT).show();
        } else { //failed to save the note! but this should not really happen :P :D :|
            Toast.makeText(this, "can not save the note. make sure you have enough space " +
                    "on your device", Toast.LENGTH_SHORT).show();
        }

        finish(); //exit the activity, should return us to MainActivity
    }
    /**
     * Handle delete action
     */
    private void actionDelete() {
        //ask user if he really wants to delete the note!
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Delete " +  mLoadedNote.getTitle())
                .setMessage("are you sure?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mLoadedNote != null && Utilities.deleteFile(getApplicationContext(), mFileName)) {
                            Toast.makeText(getApplicationContext(), mLoadedNote.getTitle() + " is deleted"
                                    , Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "can not delete the note '" + mLoadedNote.getTitle() + "'"
                                    , Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                })
                .setNegativeButton("NO", null); //do nothing on clicking NO button :P

        dialogDelete.show();
    }

    /**
     * Handle cancel action
     */
    private void actionCancel() {

        if(!checkNoteAltred()) { //if note is not altered by user (user only viewed the note/or did not write anything)
            finish(); //just exit the activity and go back to MainActivity
        } else { //we want to remind user to decide about saving the changes or not, by showing a dialog
            AlertDialog.Builder dialogCancel = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setTitle("exiting note...")
                    .setMessage("are you sure you do not want to save changes to this note?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish(); //just go back to main activity
                        }
                    })
                    .setNegativeButton("NO", null); //null = stay in the activity!
            dialogCancel.show();
        }
    }

    /**
     * Check to see if a loaded note/new note has been changed by user or not
     * @return true if note is changed, otherwise false
     */
    private boolean checkNoteAltred() {
        if(mIsUpdating || mIsViewing) { //if update mode
            return mLoadedNote != null && (!mEtTitle.getText().toString().equalsIgnoreCase(mLoadedNote.getTitle())
                    || !mEtContent.getText().toString().equalsIgnoreCase(mLoadedNote.getContent()));
        } else { //if in new note mode
            return !mEtTitle.getText().toString().isEmpty() || !mEtContent.getText().toString().isEmpty();
        }
    }

    /*
      Validate the title and content and save the note and finally exit the activity and go back to MainActivity
     */

}