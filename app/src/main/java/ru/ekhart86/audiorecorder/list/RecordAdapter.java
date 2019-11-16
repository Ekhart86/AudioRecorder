package ru.ekhart86.audiorecorder.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ru.ekhart86.audiorecorder.R;


public class RecordAdapter extends ArrayAdapter<Record> {

    private LayoutInflater inflater;
    private int layout;
    private List<Record> records;

    public RecordAdapter(Context context, int resource, List<Record> records) {
        super(context, resource, records);
        this.records = records;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View view = inflater.inflate(this.layout, parent, false);
        TextView id = view.findViewById(R.id.db_record_id);
        TextView date = view.findViewById(R.id.db_record_date_id);
        Record record = records.get(position);
        id.setText("Запись  " + record.getId());
        date.setText(record.getDate());
        return view;
    }
}
