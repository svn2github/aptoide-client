package cm.aptoide.ptdev;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.List;

import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.views.RoundedImageView;
import cm.aptoide.ptdev.webservices.timeline.ListApksInstallsRequest;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;

/**
 * Created by asantos on 01-12-2014.
 */
public class MoreFriendsInstallsActivity extends ActionBarActivity {




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home || item.getItemId() == R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.more_friends_installs));

        Fragment fragment = new MoreFriendsInstallsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

    }

    public static class MoreFriendsInstallsFragment extends Fragment {
        private RecyclerView recyclerView;

        SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);
        private int BUCKET_SIZE;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            spiceManager.start(activity);
            BUCKET_SIZE = AptoideUtils.getBucketSize();
        }


        @Override
        public void onDetach() {
            super.onDetach();
            spiceManager.shouldStop();
        }




        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_list_apps, container, false);
        }

        List<Row> list = new ArrayList<>();

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            recyclerView = (RecyclerView) view.findViewById(R.id.list);

            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));



            recyclerView.setAdapter(new FriendsInstallsAdapter(list));

            ListApksInstallsRequest listRelatedApkRequest = new ListApksInstallsRequest();

            spiceManager.execute(listRelatedApkRequest, "MoreFriendsInstallsActivity", DurationInMillis.ONE_DAY, new RequestListener<TimelineListAPKsJson>() {
                @Override
                public void onRequestFailure(SpiceException spiceException) {

                }

                @Override
                public void onRequestSuccess(TimelineListAPKsJson timelineListAPKsJson) {

                    list.clear();
                    ArrayList<TimelineListAPKsJson.UserApk> inElements = new ArrayList<TimelineListAPKsJson.UserApk>(timelineListAPKsJson.getUsersapks());

                    while (!inElements.isEmpty()) {
                        Row row = new Row();
                        for (int i = 0; i < BUCKET_SIZE && !inElements.isEmpty(); i++) {
                            row.addItem(inElements.remove(0));
                        }
                        list.add(row);
                    }

                    recyclerView.getAdapter().notifyDataSetChanged();


                }
            });
        }

    }



    public static class Row{


        private final List<TimelineListAPKsJson.UserApk> list = new ArrayList<>();


        public void addItem(TimelineListAPKsJson.UserApk userApk){

            list.add(userApk);
        }


    }



    public static class FriendsInstallsAdapter extends RecyclerView.Adapter<FriendsInstallsAdapter.TimelineRowViewHolder>{


        private final List<Row> list;

        public FriendsInstallsAdapter(List<Row> list) {
            this.list = list;
        }

        @Override
        public int getItemViewType(int position) {
            return list.get(position).list.size();
        }

        @Override
        public TimelineRowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LinearLayout inflate = new LinearLayout(parent.getContext());
            inflate.setOrientation(LinearLayout.HORIZONTAL);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            inflate.setLayoutParams(params);



            return new TimelineRowViewHolder(inflate, viewType);

        }

        @Override
        public void onBindViewHolder(TimelineRowViewHolder holder, int position) {

            final TimelineRowViewHolder viewHolder = (TimelineRowViewHolder) holder;
            Context context = viewHolder.itemView.getContext();
            int i=0;
            for(final TimelineListAPKsJson.UserApk apk : list.get(position).list) {
                TimelineRowViewHolder.ItemViewHolder itemViewHolder = (TimelineRowViewHolder.ItemViewHolder) viewHolder.views[i].getTag();
                itemViewHolder.name.setText(apk.getApk().getName());
                String icon = apk.getApk().getIcon_hd();
                if(icon == null){
                    icon = apk.getApk().getIcon();
                }
                itemViewHolder.friend.setText(apk.getInfo().getUsername() + " " + context.getString(R.string.installed_this));
                ImageLoader.getInstance().displayImage(apk.getInfo().getAvatar(), itemViewHolder.avatar);
//                Log.d("MoreFriendsInstallsActivity", "avatar: "+apk.getInfo().getAvatar());

                if(icon.contains("_icon")){
                    String[] splittedUrl = icon.split("\\.(?=[^\\.]+$)");
                    icon = splittedUrl[0] + "_" + Aptoide.iconSize  + "."+ splittedUrl[1];
                }

                ImageLoader.getInstance().displayImage(icon, itemViewHolder.icon);
                //picasso.load(icon).into(itemViewHolder.icon);
                viewHolder.views[i].setClickable(true);
                viewHolder.views[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(viewHolder.itemView.getContext(), AppViewActivity.class);
                        i.putExtra("fromRelated", true);
                        i.putExtra("md5sum", apk.getApk().getMd5sum());
                        i.putExtra("repoName", apk.getApk().getRepo());
                        i.putExtra("download_from", "recommended_apps");
                        viewHolder.itemView.getContext().startActivity(i);
                    }
                });
                i++;
            }


        }

        @Override
        public int getItemCount() {
            return list.size();
        }


        public static class TimelineRowViewHolder extends RecyclerView.ViewHolder{


            public static class ItemViewHolder {
                public TextView name;
                public TextView friend;
                public ImageView icon;
                public RoundedImageView avatar;
            }

            public LinearLayout getLinearLayout() {
                return layout;
            }

            public View[] getViews() {
                return views;
            }


            final View[] views;
            final LinearLayout layout;

            public TimelineRowViewHolder(View itemView, int viewType) {
                super(itemView);

                views = new View[viewType];

                layout = (LinearLayout) itemView;
                for(int i = 0; i < viewType; i++){
                    View inflate = LayoutInflater.from(itemView.getContext()).inflate(R.layout.timeline_item, layout, false);
                    views[i] = inflate;
                    ItemViewHolder holder = new ItemViewHolder();
                    holder.name = (TextView) inflate.findViewById(R.id.app_name);
                    holder.icon = (ImageView) inflate.findViewById(R.id.app_icon);
                    holder.friend = (TextView) inflate.findViewById(R.id.app_friend);
                    holder.avatar = (RoundedImageView) inflate.findViewById(R.id.user_avatar);

                    inflate.setTag(holder);
                    layout.addView(inflate);

                }


            }

        }



    }



}
