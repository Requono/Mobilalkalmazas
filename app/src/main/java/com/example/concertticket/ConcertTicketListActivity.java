package com.example.concertticket;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ConcertTicketListActivity extends AppCompatActivity {

    private static final String LOG_TAG = ConcertTicketListActivity.class.getName();

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private RecyclerView mRecyclerView;
    private ArrayList<ConcertTicket> itemList;
    private ConcertTicketAdapter concertTicketAdapter;

    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;
    private DocumentReference mItems;

    private NotificationHandler handler;
    private JobScheduler jobScheduler;

    private boolean viewRow = true;
    private int gridNumber = 1;
    private int queryLimit = 5;

    String[] itemTitle;
    String[] itemDesc;
    String[] itemPrice;
    TypedArray itemImageResource;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concert_ticket_list);
        user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        //mAuth.signOut();

        if(user != null) {
            Log.d(LOG_TAG, "Authenticated user");
        } else {
            Log.d(LOG_TAG, "Unauthenticated user");
        }


        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        itemList = new ArrayList<>();
        concertTicketAdapter = new ConcertTicketAdapter(this, itemList);
        mRecyclerView.setAdapter(concertTicketAdapter);

        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("TicketList");
        queryData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(receiver, filter);

        handler = new NotificationHandler(this);

        jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        //setJobScheduler();

    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action == null) return;

            switch(action) {
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit = 5;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit = 4;
                    break;
            }
            queryData();
        }
    };

    private void queryData() {

        itemList.clear();

        if(itemList.size() == 0) {
            initializeData();
        }

        Log.d(LOG_TAG, "asd not gud");

        collectionReference.limit(queryLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                ConcertTicket ticket = doc.toObject(ConcertTicket.class);
                ticket.setId(doc.getId());
                itemList.add(ticket);
            }

            concertTicketAdapter.notifyDataSetChanged();
        })
        .addOnFailureListener(e -> Log.e(LOG_TAG, "asd"));

    }

    public void deleteItem(ConcertTicket ticket) {
        DocumentReference ref = collectionReference.document(ticket._getId());
        ref.delete().addOnSuccessListener(success -> {
            Log.d(LOG_TAG, "Item is deleted" + ticket._getId());
        })
        .addOnFailureListener(failure -> {
            Toast.makeText(this, "Item" + ticket._getId() + "cannot be deleted", Toast.LENGTH_LONG).show();
        });

        handler.send("Meh, but that would have been a good concert for you :(");

        queryData();
    }

    private void initializeData() {
        itemTitle = getResources().getStringArray(R.array.concert_ticket_title);
        itemDesc = getResources().getStringArray(R.array.concert_ticket_description);
        itemPrice = getResources().getStringArray(R.array.concert_ticket_price);
        itemImageResource = getResources().obtainTypedArray(R.array.concert_ticket_images);

        for( int i = 0; i < itemTitle.length; i++) {
            collectionReference.add(new ConcertTicket(itemTitle[i], itemDesc[i], itemPrice[i], itemImageResource.getResourceId(i, 0)));
        }

        itemImageResource.recycle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.concert_ticket_menu, menu);
        MenuItem item = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(LOG_TAG, newText);
                concertTicketAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.cart:
                return true;
            case R.id.view_selector:
                if(viewRow) {
                    changeSpanCount(item, R.drawable.ic_view_grid, 1);
                } else {
                    changeSpanCount(item, R.drawable.ic_view_row, 2);
                }
            case R.id.settings_button:
                return true;
            case R.id.log_out:
                FirebaseAuth.getInstance().signOut();
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeSpanCount(MenuItem item, int drawable, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawable);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setJobScheduler() {
        int type = JobInfo.NETWORK_TYPE_UNMETERED;
        int deadLine = 2500;

        ComponentName name = new ComponentName(getPackageName(), NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(0, name)
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(type)
                .setOverrideDeadline(deadLine);

        jobScheduler.schedule(builder.build());
    }

}