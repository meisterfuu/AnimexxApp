package de.meisterfuu.animexx.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.meisterfuu.animexx.R;
import de.meisterfuu.animexx.objects.xmpp.XMPPRoosterObject;
import de.meisterfuu.animexx.utils.imageloader.PicassoDownloader;


public class XMPPRoosterAdapter extends BaseAdapter {

    List<XMPPRoosterObject> mItems;
    Activity mContext;

    public XMPPRoosterAdapter(List<XMPPRoosterObject> pList, Activity pContext) {
        this.mItems = pList;
        this.mContext = pContext;
    }

    boolean mLoading;

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void startLoadingAnimation() {
        mLoading = true;
        this.notifyDataSetChanged();
    }

    public void stopLoadingAnimation() {
        mLoading = false;
        this.notifyDataSetChanged();
    }

    public boolean isLoadingAnimation() {
        return mLoading;
    }

    static class ViewHolder {

        public TextView Title, Subtitle;
        public ImageView Avatar;
        public TextView Message;
        public ImageView StatusIcon;
    }

    @Override
    public int getCount() {
        if (isLoadingAnimation()) {
            return mItems.size() + 1;
        }
        return mItems.size();
    }


    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public XMPPRoosterObject getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();

            rowView = inflater.inflate(R.layout.listitem_rooster, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.Title = (TextView) rowView.findViewById(R.id.xmpp_list_item_title);
            viewHolder.Subtitle = (TextView) rowView.findViewById(R.id.xmpp_list_item_subtitle);
            viewHolder.Message = (TextView) rowView.findViewById(R.id.xmpp_list_item_msg);
            viewHolder.Avatar = (ImageView) rowView.findViewById(R.id.xmpp_list_item_avatar);
            viewHolder.StatusIcon = (ImageView) rowView.findViewById(R.id.xmpp_list_item_status_icon);

            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        final XMPPRoosterObject xmpp = mItems.get(position);

        //Subject
        holder.Title.setText(xmpp.toString());


        if (xmpp.getStatus() == XMPPRoosterObject.STATUS_ONLINE) {
            holder.Subtitle.setText("Online");
            holder.StatusIcon.setImageResource(R.drawable.chat_status_marker_online);
        } else if (xmpp.getStatus() == XMPPRoosterObject.STATUS_AWAY) {
            holder.Subtitle.setText("Abwesend");
            holder.StatusIcon.setImageResource(R.drawable.chat_status_marker_away);
        } else {
            holder.Subtitle.setText("Offline");
            holder.StatusIcon.setImageResource(R.drawable.chat_status_marker_offline);
        }


        //Avatar

        holder.Avatar.setVisibility(View.VISIBLE);
        PicassoDownloader.getAvatarPicasso(mContext)
                .load(PicassoDownloader.getAvatarURI(PicassoDownloader.createAvatarKey(xmpp.getAnimexxID()), mContext))
                .stableKey(PicassoDownloader.createAvatarKey(xmpp.getAnimexxID()))
                .placeholder(R.drawable.ic_contact_picture)
                .error(R.drawable.ic_contact_picture)
                .into(holder.Avatar);

        if (xmpp.latestMessage != null) {
            if (xmpp.latestMessage.isMe()) {
                holder.Message.setText("> " + xmpp.latestMessage.getBody().subSequence(0, Math.min(30, xmpp.latestMessage.getBody().length())));
            } else {
                holder.Message.setText("< " + xmpp.latestMessage.getBody().subSequence(0, Math.min(30, xmpp.latestMessage.getBody().length())));
            }

        } else {
            holder.Message.setText("");
        }

        return rowView;
    }

    public void addAll(ArrayList<XMPPRoosterObject> list) {
        mItems.addAll(list);
        this.notifyDataSetChanged();
    }


}
