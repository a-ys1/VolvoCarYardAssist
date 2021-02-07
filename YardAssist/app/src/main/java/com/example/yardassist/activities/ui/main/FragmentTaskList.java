package com.example.yardassist.activities.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.yardassist.R;
import com.example.yardassist.activities.NewTaskActivity;
import com.example.yardassist.classes.Taskitem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FragmentTaskList extends Fragment {

    public static void getAllFromFireStore(OnCompleteListener<QuerySnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks").get().addOnCompleteListener(listener);
    }

    ListView taskList;
    ArrayList<Taskitem> itemList = new ArrayList<>();
    FragmentTaskList.TaskAdapter taskAdapter;
    FirebaseFirestore dbRef = FirebaseFirestore.getInstance();
    CollectionReference colRef = dbRef.collection("tasks");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_list, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskList = getView().findViewById(R.id.taskList);
        taskAdapter = new TaskAdapter(itemList, getContext());
        taskList.setAdapter(taskAdapter);
        dbRef.collection("tasks").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("TAG", "listen:error", error);
                    return;
                }
                else
                {
                    itemList.clear();
                    listFiller();
                }
            }
        });

        final ImageButton taskButton = getView().findViewById(R.id.taskButton);
        taskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),NewTaskActivity.class);
                intent.putExtra("activity_ID", "TaskListFragment");
                startActivity(intent);
            }
        });

        taskList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                popTask(itemList.get(position));
            }


        });

    }


    public void popTask(Taskitem taskitem){

        poptask popTask=new poptask(taskitem);
        popTask.show(getActivity().getSupportFragmentManager(),"hello");

    }


    public void listFiller() {

        getAllFromFireStore(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("Project3", document.getId() + " => " + document.getData());
                        String id = document.get("id") + "";
                        String time = document.get("time") +"";
                        String comment = document.get("comment") + "";
                        int col = Integer.parseInt(document.get("col").toString().trim());
                        int row = Integer.parseInt(document.get("row").toString().trim());


                        Taskitem item = new Taskitem(id, time, comment, col, row);
                        if(itemList.contains(item))
                        {
                            return;
                        }
                        else {
                            itemList.add(item);
                            taskAdapter.notifyDataSetChanged();
                        }
                    }

                } else {
                    Log.d("ERROR", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
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
            TextView time = view.findViewById(R.id.timePosted);
            TextView itemComment = view.findViewById(R.id.itemComment);

            vID.setText(taskItemListFiltered.get(i).getId());
            time.setText(taskItemListFiltered.get(i).getTime());
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
                            if(taskitem.getId().toLowerCase().contains(searchStr) || taskitem.getComment().toLowerCase().contains(searchStr)){
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
                    taskAdapter.notifyDataSetChanged();
                }
            };
            return filter;
        }
    }


}