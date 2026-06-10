/*
 * ᴄʜᴀᴛꜱꜰʀᴀɢᴍᴇɴᴛ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ᴄʜᴀᴛꜱ ᴛᴀʙ ᴡɪᴛʜ ꜰʀᴏꜱᴛᴇᴅ ɢʟᴀꜱꜱ ᴄʜᴀᴛ ʟɪꜱᴛ ɪᴛᴇᴍꜱ
 */

package com.alternative.telegram;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;

    public static ChatsFragment newInstance() {
        return new ChatsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<ChatItem> items = generateSampleChats();
        adapter = new ChatAdapter(items);
        recyclerView.setAdapter(adapter);

        TextView emptyText = view.findViewById(R.id.emptyText);
        if (items.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            MiniFontConverter.apply(emptyText);
            emptyText.setText(MiniFontConverter.convert("ɴᴏ ᴄʜᴀᴛꜱ ʏᴇᴛ"));
        } else {
            emptyText.setVisibility(View.GONE);
        }

        return view;
    }

    private List<ChatItem> generateSampleChats() {
        List<ChatItem> items = new ArrayList<>();
        items.add(new ChatItem("ᴊᴏʜɴ ᴅᴏᴇ", "ʜᴇʏ, ᴀʀᴇ ʏᴏᴜ ᴄᴏᴍɪɴɢ ᴛᴏɴɪɢʜᴛ?", "10:42", 2, false));
        items.add(new ChatItem("ᴛᴇᴄʜ ɴᴇᴡꜱ ᴄʜᴀɴɴᴇʟ", "ɴᴇᴡ ᴀɴᴅʀᴏɪᴅ ᴜᴘᴅᴀᴛᴇ ʀᴏʟʟᴇᴅ ᴏᴜᴛ...", "09:15", 0, true));
        items.add(new ChatItem("ꜱᴀʀᴀʜ ꜱᴍɪᴛʜ", "ᴛʜᴀɴᴋꜱ ꜰᴏʀ ʏᴏᴜʀ ʜᴇʟᴘ!", "ʏᴇꜱᴛᴇʀᴅᴀʏ", 0, false));
        items.add(new ChatItem("ᴘʀᴏᴊᴇᴄᴛ ᴛᴇᴀᴍ", "ᴀʟɪᴄᴇ: ɪ'ʟʟ ᴘᴜꜱʜ ᴛʜᴇ ᴄᴏᴅᴇ...", "ʏᴇꜱᴛᴇʀᴅᴀʏ", 5, false));
        items.add(new ChatItem("ᴍɪᴋᴇ ʀᴏꜱꜱ", "ᴄᴀɴ ʏᴏᴜ ꜱᴇɴᴅ ᴛʜᴇ ꜰɪʟᴇ?", "ᴍᴏɴ", 1, false));
        return items;
    }

    // ᴄʜᴀᴛ ɪᴛᴇᴍ ᴅᴀᴛᴀ ᴄʟᴀꜱꜱ
    public static class ChatItem {
        public final String name;
        public final String preview;
        public final String time;
        public final int unreadCount;
        public final boolean isChannel;

        public ChatItem(String name, String preview, String time, int unreadCount, boolean isChannel) {
            this.name = name;
            this.preview = preview;
            this.time = time;
            this.unreadCount = unreadCount;
            this.isChannel = isChannel;
        }
    }

    // ʀᴇᴄʏᴄʟᴇʀᴠɪᴇᴡ ᴀᴅᴀᴘᴛᴇʀ
    static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

        private final List<ChatItem> items;

        ChatAdapter(List<ChatItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatItem item = items.get(position);
            holder.nameText.setText(MiniFontConverter.convert(item.name));
            holder.previewText.setText(MiniFontConverter.convert(item.preview));
            holder.timeText.setText(MiniFontConverter.convert(item.time));

            if (item.unreadCount > 0) {
                holder.unreadBadge.setVisibility(View.VISIBLE);
                holder.unreadBadge.setText(MiniFontConverter.convert(
                        String.valueOf(item.unreadCount)));
            } else {
                holder.unreadBadge.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView nameText;
            final TextView previewText;
            final TextView timeText;
            final TextView unreadBadge;

            ViewHolder(View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.chatName);
                previewText = itemView.findViewById(R.id.chatPreview);
                timeText = itemView.findViewById(R.id.chatTime);
                unreadBadge = itemView.findViewById(R.id.unreadBadge);
            }
        }
    }
}
