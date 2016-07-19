package com.example.aanya.todolist;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.aanya.todolist.db.TaskTable;
import com.example.aanya.todolist.models.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView taskListView;
    private ArrayList<Task> taskList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskListView = (ListView) findViewById(R.id.list_task);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        updateUI();

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                deleteTaskSelected();
                updateUI();
                swipeRefresh.setRefreshing(false);
            }
        });

//        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//                TextView tvTitleDone = (TextView)view.findViewById(R.id.task_title);
//                tvTitleDone.setPaintFlags(tvTitleDone.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                TextView tvDateDone = (TextView) view.findViewById(R.id.task_date);
//                tvDateDone.setPaintFlags(tvDateDone.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//
//                //deleteTask(taskList.get(i).getId());
//            }
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_add_task:
                LayoutInflater li = LayoutInflater.from(this);
                final View alertDialogView = li.inflate(R.layout.alert_dialog, null);
                final EditText etTaskTitleAD = (EditText) alertDialogView.findViewById(R.id.et_task_title_ad);
                final DatePicker dpTaskDateAd = (DatePicker) alertDialogView.findViewById(R.id.dp_task_date_ad);
                dpTaskDateAd.setMinDate(System.currentTimeMillis() - 1000);
                final AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setTitle("Add Task");
                alert.setMessage("What's more?");
                alert.setView(alertDialogView);
                alert.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        SQLiteDatabase myDb = MyDbOpener.openWritableDatabase(MainActivity.this);
                        ContentValues values = new ContentValues();

                        String taskTitle = etTaskTitleAD.getText().toString();
                        int year = dpTaskDateAd.getYear();
                        int month = dpTaskDateAd.getMonth() + 1;
                        int day = dpTaskDateAd.getDayOfMonth();
                        String taskDate = year + "-";
                        if (month < 10) {
                            taskDate += "0" + month + "-";
                        } else {
                            taskDate += month + "-";
                        }
                        if (day < 10) {
                            taskDate += "0" + day;
                        } else {
                            taskDate += day;
                        }
                        values.put(TaskTable.Columns.TITLE, taskTitle);
                        values.put(TaskTable.Columns.DATE, taskDate);
                        myDb.insert(TaskTable.TABLE_NAME, null, values);
                        myDb.close();

                        updateUI();

                    }
                });
                alert.setNegativeButton("CANCEL", null);
                alert.create();
                alert.show();

                return true;

            case R.id.action_refresh:
                //swipeRefresh.setRefreshing(true);
                deleteTaskSelected();
                updateUI();
                //swipeRefresh.setRefreshing(false);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }


    }

    private class TaskListAdapter extends BaseAdapter {
        ArrayList<Task> mTasks = new ArrayList<>();

        public TaskListAdapter(ArrayList<Task> mTasks) {
            this.mTasks = mTasks;
        }

        public class TaskHolder {
            TextView titleView;
            TextView dateView;
            CheckBox cbSelect;
            ImageButton btnEdit;
        }

        @Override
        public int getCount() {
            return mTasks.size();
        }

        @Override
        public Object getItem(int i) {
            return mTasks.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater layoutInflater = getLayoutInflater();
            TaskHolder taskHolder = new TaskHolder();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.list_item, null);
                taskHolder.titleView = (TextView) view.findViewById(R.id.task_title);
                taskHolder.dateView = (TextView) view.findViewById(R.id.task_date);
                taskHolder.cbSelect = (CheckBox) view.findViewById(R.id.check_box);
                taskHolder.btnEdit = (ImageButton) view.findViewById(R.id.btn_edit);
                view.setTag(taskHolder);

                taskHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ImageButton ib = (ImageButton) view;
                        Task task = (Task) ib.getTag();
                        editTask(task);
                    }
                });

                taskHolder.cbSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CheckBox cb = (CheckBox) view;
                        ViewGroup vg = (ViewGroup) view.getParent();
                        TextView tvTask = (TextView) vg.findViewById(R.id.task_title);
                        TextView tvDate = (TextView) vg.findViewById(R.id.task_date);
                        if(cb.isChecked()){
                            tvTask.setPaintFlags(tvTask.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            tvDate.setPaintFlags(tvDate.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        }else{
                            tvTask.setPaintFlags(tvTask.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                            tvDate.setPaintFlags(tvDate.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        }
                        Task task = (Task) cb.getTag();
                        task.setSelected(cb.isChecked());
                    }
                });

            } else {
                taskHolder = (TaskHolder) view.getTag();
            }
            Task thisTask = mTasks.get(i);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String todayDate = sdf.format(new Date());
            Log.d(TAG, "getView: " + todayDate);

            taskHolder.titleView.setText(thisTask.getTitle());
            taskHolder.dateView.setText(parseDate(thisTask.getDate()));
            taskHolder.cbSelect.setChecked(thisTask.isSelected());
            taskHolder.cbSelect.setTag(thisTask);
            taskHolder.btnEdit.setTag(thisTask);

            if (thisTask.getDate().compareTo(todayDate) < 0) {
                taskHolder.titleView.setTextColor(Color.RED);
                taskHolder.dateView.setTextColor(Color.RED);
            } else {
                taskHolder.titleView.setTextColor(Color.BLACK);
                taskHolder.dateView.setTextColor(Color.BLACK);
            }
            return view;
        }
    }

    private void updateUI() {
        taskList.clear();
        SQLiteDatabase thisDb = MyDbOpener.openReadableDatabase(this);

        String projection[] = {
                TaskTable.Columns.ID,
                TaskTable.Columns.TITLE,
                TaskTable.Columns.DATE
        };

        Cursor c = thisDb.query(
                TaskTable.TABLE_NAME,
                projection,
                null, null, null, null, TaskTable.Columns.DATE + " asc"
        );

        while (c.moveToNext()) {
            int thisIndex = c.getInt(c.getColumnIndex(TaskTable.Columns.ID));
            String thisTitle = c.getString(c.getColumnIndex(TaskTable.Columns.TITLE));
            String thisDate = c.getString(c.getColumnIndex(TaskTable.Columns.DATE));
            Task newTask = new Task(thisIndex, thisTitle, thisDate);
            taskList.add(newTask);
        }

        TaskListAdapter taskListAdapter = new TaskListAdapter(taskList);
        taskListAdapter.notifyDataSetChanged();
        taskListView.setAdapter(taskListAdapter);

        c.close();
        thisDb.close();

    }

    public void deleteTaskSelected() {
        SQLiteDatabase deleteDb = MyDbOpener.openWritableDatabase(this);
        String selection = TaskTable.Columns.ID + " = ?";

        for (int i = 0; i < taskList.size(); i++) {
            Task taskSel = taskList.get(i);
            if (taskSel.isSelected()) {
                String[] selectionArgs = {String.valueOf(taskSel.getId())};
                deleteDb.delete(TaskTable.TABLE_NAME, selection, selectionArgs);
            }
        }
        deleteDb.close();
    }

    public void deleteTask(int deleteId) {
        SQLiteDatabase deleteDb = MyDbOpener.openWritableDatabase(this);
        String selection = TaskTable.Columns.ID + " = ?";
        String[] selectionArgs = {String.valueOf(deleteId)};
        deleteDb.delete(TaskTable.TABLE_NAME, selection, selectionArgs);
        deleteDb.close();
    }

    public void editTask(final Task editTask) {

        LayoutInflater li = LayoutInflater.from(this);
        final View alertDialogView = li.inflate(R.layout.alert_dialog, null);
        final EditText etTaskTitleAD = (EditText) alertDialogView.findViewById(R.id.et_task_title_ad);
        final DatePicker dpTaskDateAd = (DatePicker) alertDialogView.findViewById(R.id.dp_task_date_ad);

        etTaskTitleAD.setText(editTask.getTitle());
        dpTaskDateAd.setMinDate(System.currentTimeMillis() - 1000);
        etTaskTitleAD.setSelection(etTaskTitleAD.getText().length());

        String dateSet[] = editTask.getDate().split("-");
        dpTaskDateAd.updateDate(Integer.parseInt(dateSet[0]), Integer.parseInt(dateSet[1]) - 1, Integer.parseInt(dateSet[2]));


        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Update Task");
        alert.setMessage("What's more?");
        alert.setView(alertDialogView);
        alert.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                SQLiteDatabase myDb = MyDbOpener.openWritableDatabase(MainActivity.this);
                ContentValues values = new ContentValues();

                String taskTitle = etTaskTitleAD.getText().toString();
                int year = dpTaskDateAd.getYear();
                int month = dpTaskDateAd.getMonth() + 1;
                int day = dpTaskDateAd.getDayOfMonth();
                String taskDate = year + "-";
                if (month < 10) {
                    taskDate += "0" + month + "-";
                } else {
                    taskDate += month + "-";
                }
                if (day < 10) {
                    taskDate += "0" + day;
                } else {
                    taskDate += day;
                }
                values.put(TaskTable.Columns.TITLE, taskTitle);
                values.put(TaskTable.Columns.DATE, taskDate);
                String selection = TaskTable.Columns.ID + " = ?";
                String[] selectionArgs = {String.valueOf(editTask.getId())};
                myDb.update(
                        TaskTable.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                myDb.close();
                updateUI();

            }
        });
        alert.setNegativeButton("CANCEL", null);
        alert.create();
        alert.show();
    }

    public String parseDate(String time) {
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd MMM, yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
        Date date = null;
        String str = null;
        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

}
