package com.laioffer.eventreporter;

import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity implements EventFragment.OnItemSelectListener {
    private static final String TAG = "MainActivity";

    private EventFragment mListFragment;
    private CommentFragment mGridFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get ListView object from xml.
//        ListView eventListView = (ListView) findViewById(R.id.event_list);

        // Initialize an adapter.
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//                this,
//                R.layout.event_item,
//                R.id.event_name,
//                getEventNames());

//        EventAdapter adapter = new EventAdapter(this);

        // Assign adapter to ListView.
//        eventListView.setAdapter(adapter);

        /* Add ListView */
        mListFragment = new EventFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.event_container, mListFragment)
                .commit();

        /* Add GridView */
        if (isTablet()) {
            mGridFragment = new CommentFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.comment_container, mGridFragment)
                    .commit();
        }

        /* Show different fragments based on screen size
        *  Check whether the device is phone or tablet
        */
//        LinearLayout layout = findViewById(R.id.fragment_container);
//        if (layout != null) {
//            Fragment fragment = isTablet() ? new CommentFragment() : new EventFragment();
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .add(R.id.fragment_container, fragment)
//                    .commit();
//        }
    }

    private boolean isTablet() {
        boolean res = (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        Log.d(TAG, "Is tablet: " + res);
        return res;
    }

    /**
     * A dummy function to get fake event names.
     *
     * @return an array of fake event names.
     */
//    private String[] getEventNames() {
//        String[] names = {
//                "Event1", "Event2", "Event3",
//                "Event4", "Event5", "Event6",
//                "Event7", "Event8", "Event9",
//                "Event10", "Event11", "Event12"};
//        return names;
//    }

    @Override
    protected void onPause() {
        super.onPause();
        //important
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if(isChangingConfigurations() && fragment != null) {
            fragmentManager.beginTransaction().remove(fragment).commit();
//            Log.e("Life cycle test", "Remove fragment");
        }
        Log.e("MainActivity", "We are at onPause()");
    }

    @Override
    public void onItemSelected(int position) {
        mGridFragment.onItemSelected(position);
    }
}
