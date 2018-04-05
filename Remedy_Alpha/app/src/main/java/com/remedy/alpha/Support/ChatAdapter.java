package com.remedy.alpha.Support;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.remedy.alpha.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.remedy.alpha.model.RemedyMessage;

public class ChatAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext;
    private List<RemedyMessage> mMessageList;

    public ChatAdapter(Context context, List<RemedyMessage> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        RemedyMessage message = (RemedyMessage) mMessageList.get(position);

        if (message.isSentByCustomer()) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RemedyMessage message = (RemedyMessage) mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.sent_text_message_body);
//            timeText = (TextView) itemView.findViewById(R.id.sent_text_message_time);
        }

        void bind(RemedyMessage message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
//            timeText.setText(new SimpleDateFormat("h:mm a").format(message.getSendDate()));
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.received_text_message_body);
//            timeText = (TextView) itemView.findViewById(R.id.received_text_message_time);
//            nameText = (TextView) itemView.findViewById(R.id.received_text_message_name);
            profileImage = (ImageView) itemView.findViewById(R.id.received_image_message_profile);
        }

        void bind(RemedyMessage message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
//            timeText.setText(new SimpleDateFormat("h:mm a").format(message.getSendDate()));


//            nameText.setText(message.getAgentID());


            // Insert the profile image from the URL into the ImageView.
//            Utils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUrl(), profileImage);
        }
    }
}