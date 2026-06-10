/*
 * ɢʀᴏᴜᴘꜱꜰʀᴀɢᴍᴇɴᴛ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ɢʀᴏᴜᴘꜱ ᴛᴀʙ
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

public class GroupsFragment extends Fragment {

    private RecyclerView recyclerView;

    public static GroupsFragment newInstance() {
        return new GroupsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<ChatsFragment.ChatItem> items = generateSampleGroups();
        recyclerView.setAdapter(new ChatsFragment.ChatAdapter(items));

        TextView emptyText = view.findViewById(R.id.emptyText);
        emptyText.setText(MiniFontConverter.convert("ɴᴏ ɢʀᴏᴜᴘꜱ ʏᴇᴛ"));
        emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        return view;
    }

    private List<ChatsFragment.ChatItem> generateSampleGroups() {
        List<ChatsFragment.ChatItem> items = new ArrayList<>();
        items.add(new ChatsFragment.ChatItem("ᴅᴇᴠ ᴛᴇᴀᴍ", "ᴍɪᴋᴇ: ᴘʀ ᴄʀᴇᴀᴛᴇᴅ ꜰᴏʀ ɴᴇᴡ ꜰᴇᴀᴛᴜʀᴇ", "11:20", 3, false));
        items.add(new ChatsFragment.ChatItem("ɢᴀᴍɪɴɢ ᴄʀᴇᴡ", "ᴀɴʏᴏɴᴇ ᴜᴘ ꜰᴏʀ ᴀ ʀᴀɪᴅ?", "ʟᴀꜱᴛ ᴡᴇᴇᴋ", 0, false));
        return items;
    }
}
