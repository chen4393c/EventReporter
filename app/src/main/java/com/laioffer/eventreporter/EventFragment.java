package com.laioffer.eventreporter;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A simple {@link Fragment} subclass.
 */
public class EventFragment extends Fragment {
    OnItemSelectListener mCallback;

    public interface OnItemSelectListener {
        public void onItemSelected(int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnItemSelectListener) context;
        } catch (ClassCastException e) {
            // do something
        }
    }

    String name;

//    Normal constructor/ newInstance
//    Recreated will use the standard constructor

    public EventFragment() {
        // Required empty public constructor
    }

    public EventFragment newInstance() {
        EventFragment fragment = new EventFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name", "test");
        fragment.setArguments(bundle);
        return fragment;
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            name = savedInstanceState.getString("name");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        ListView listView = (ListView) view.findViewById(R.id.event_list);
//        listView.setAdapter(new EventAdapter(getActivity()));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                getEventNames()
        );
        // Assign adapter to ListView
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCallback.onItemSelected(i);
            }
        });
        return view;
    }


    private String[] getEventNames() {
        String[] names = {
                "Event1", "Event2", "Event3",
                "Event4", "Event5", "Event6",
                "Event7", "Event8", "Event9",
                "Event10", "Event11", "Event12"
        };
        return names;
    }
}
