package de.meisterfuu.animexx.activitys.profiles;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.meisterfuu.animexx.R;
import de.meisterfuu.animexx.adapter.ProfileTableAdapter;
import de.meisterfuu.animexx.api.broker.UserBroker;
import de.meisterfuu.animexx.api.web.ReturnObject;
import de.meisterfuu.animexx.objects.profile.ProfileBoxObject;
import de.meisterfuu.animexx.utils.views.FeedbackListView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProfilePageTableFragment extends Fragment {


    private UserBroker mApi;
    private String mBoxID;
    private long mID;
    private FeedbackListView mListView;
    private ProfileTableAdapter mAdapter;
    private List<List<String>> mList;

    public static Fragment getInstance(Context pContext, long pUserID, String pBoxID) {
        ProfilePageTableFragment fragment = new ProfilePageTableFragment();
        Bundle args = new Bundle();
        args.putLong("id", pUserID);
        args.putString("boxid", pBoxID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_page_fan, container, false);

        if (getArguments() != null) {
            mID = getArguments().getLong("id");
            mBoxID = getArguments().getString("boxid");
        }

        mApi = new UserBroker(this.getActivity());

        mListView = (FeedbackListView) v.findViewById(android.R.id.list);
        mList = new ArrayList<List<String>>();
        mAdapter = new ProfileTableAdapter(mList, this.getActivity());
        mListView.setAdapter(mAdapter);
        load();

        return v;

    }

    private void load() {
        mListView.showLoading();
        mApi.getProfileBox(mBoxID, mID, new Callback<ReturnObject<ProfileBoxObject>>() {
            @Override
            public void success(ReturnObject<ProfileBoxObject> o, Response response) {
                mListView.showList();
                ProfileBoxObject obj = (ProfileBoxObject) o.getObj();
                mAdapter.addAll(obj.getDataList());
                if(mAdapter.getCount() == 0){
                    mListView.showError("Keine Einträge");
                }
            }

            @Override
            public void failure(RetrofitError error) {
                mListView.showError("Es ist ein Fehler aufgetreten");
            }
        });

    }
}
