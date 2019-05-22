package com.app.ben.notetaker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class NoteListAdapter extends ArrayAdapter<Note> implements Filterable {
    List<Note> objects;
    private List<Note> originalList = new ArrayList<>();
    Filter filter;
    private static final int WRAP_CONTENT_LENGTH = 5;

    public NoteListAdapter(Context context, int resource, List<Note> objects) {
        super(context, resource, objects);
        this.objects = objects;
        this.originalList.addAll(objects);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Nullable
    @Override
    public Note getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public Filter getFilter() {
        filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<Note> tempList = new ArrayList<>();
                FilterResults results = new FilterResults();
                if (objects != null) {
                    if(constraint != null && !constraint.equals("")) {
                        for (Note singleNote : objects) {
                            if (singleNote.getTitle().contains(constraint))
                                tempList.add(singleNote);
                        }
                        results.values = tempList;
                        results.count = tempList.size();
                    }else {
                        tempList.clear();
                        tempList.addAll(originalList);
                        results.values = tempList;
                        results.count = tempList.size();
                    }
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                objects = (ArrayList<Note>) results.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_component, parent, false);
        }

        Note note = getItem(position);

        if(note != null) {
            TextView title = (TextView) convertView.findViewById(R.id.list_note_title);
            TextView content = (TextView) convertView.findViewById(R.id.list_note_content_preview);
            TextView date = (TextView) convertView.findViewById(R.id.list_note_date);
            title.setText(note.getTitle());
            date.setText(note.getDateTimeFormatted(getContext()));

            //correctly show preview of the content (not more than 5 char )
            int toWrap = WRAP_CONTENT_LENGTH;
            if(note.getContent().length() > WRAP_CONTENT_LENGTH) {
                if(toWrap > 0) {
                    content.setText(note.getContent().substring(0, toWrap) + "...");
                } else {
                    content.setText(note.getContent());
                }
            }
        }

        return convertView;
    }


}
