/*package com.example.yardassist.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.yardassist.R;
import com.example.yardassist.classes.Taskitem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class ManagerActivity extends AppCompatActivity {

    FirebaseFirestore dbRef;
    Button taskButton;
    ListView taskList;
    private Object OnCompleteListener;
    ArrayList<Taskitem> itemList = new ArrayList<>();
    TaskAdapter taskAdapter;

    public static void getAllFromFireStore(OnCompleteListener<QuerySnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks").get().addOnCompleteListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        taskList = findViewById(R.id.taskList);
        listFiller();
        taskAdapter = new TaskAdapter(itemList, this);
        taskList.setAdapter(taskAdapter);

        final Button taskButton = findViewById(R.id.taskButton);
        taskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerActivity.this, NewTaskActivity.class);
                startActivity(intent);
            }
        });
    }

    public void listFiller() {

        getAllFromFireStore(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Project3", document.getId() + " => " + document.getData());
                                String id = document.get("id") + "";
                                double latitude = Double.parseDouble(document.get("latitude").toString().trim());
                                double longitude = Double.parseDouble(document.get("longitude").toString().trim());
                                String comment = document.get("comment") + "";

                                Taskitem item = new Taskitem(id, latitude, longitude, comment);
                                itemList.add(item);
                                taskAdapter.notifyDataSetChanged();
                            }

                        } else {
                            Log.d("ERROR", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

   public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem menuItem = menu.findItem(R.id.search_view);
        final SearchView searchView = (SearchView)menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                taskAdapter.getFilter().filter(s);

                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.search_view){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class TaskAdapter extends BaseAdapter implements Filterable {

        private List<Taskitem> taskItemList;
        private List<Taskitem> taskItemListFiltered;
        private Context context;

        public TaskAdapter(List<Taskitem> taskItemList, Context context) {
            this.taskItemList = taskItemList;
            this.taskItemListFiltered = taskItemList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return taskItemListFiltered.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View view = getLayoutInflater().inflate(R.layout.item_row, null);

            TextView vID = view.findViewById(R.id.vID);
            TextView latitude = view.findViewById(R.id.latitude);
            TextView longitude = view.findViewById(R.id.longitude);
            TextView itemComment = view.findViewById(R.id.itemComment);

            vID.setText(taskItemListFiltered.get(i).getId());
            latitude.setText(Double.toString(taskItemListFiltered.get(i).getLatitude()));
            longitude.setText(Double.toString(taskItemListFiltered.get(i).getLongitude()));
            itemComment.setText(taskItemListFiltered.get(i).getComment());
            return view;
        }

        @Override
        public Filter getFilter() {

            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    FilterResults filterResults = new FilterResults();

                    if(charSequence == null || charSequence.length() == 0){
                        filterResults.count = taskItemList.size();
                        filterResults.values = taskItemList;
                    }
                    else{
                        String searchStr = charSequence.toString().toLowerCase();
                        List<Taskitem> resultData = new ArrayList<>();

                        for(Taskitem taskitem:taskItemList){
                            if(taskitem.getId().contains(searchStr) || taskitem.getComment().contains(searchStr)){
                                resultData.add(taskitem);
                            }

                            filterResults.count = resultData.size();
                            filterResults.values = resultData;
                        }
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults results) {
                    taskItemListFiltered = (List<Taskitem>)results.values;
                    notifyDataSetChanged();
                }
            };
            return filter;
        }
    }


}*/