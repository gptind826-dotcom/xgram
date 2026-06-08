/*
 * ᴄᴏɴᴛᴀᴄᴛꜱꜰʀᴀɢᴍᴇɴᴛ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ᴄᴏɴᴛᴀᴄᴛꜱ ᴛᴀʙ
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

public class ContactsFragment extends Fragment {

    private RecyclerView recyclerView;

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<ChatsFragment.ChatItem> items = generateSampleContacts();
        recyclerView.setAdapter(new ChatsFragment.ChatAdapter(items));

        TextView emptyText = view.findViewById(R.id.emptyText);
        emptyText.setText(MiniFontConverter.convert("ɴᴏ ᴄᴏɴᴛᴀᴄᴛꜱ ʏᴇᴛ"));
        emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        return view;
    }

    private List<ChatsFragment.ChatItem> generateSampleContacts() {
        List<ChatsFragment.ChatItem> items = new ArrayList<>();
        items.add(new ChatsFragment.ChatItem("ᴀʟɪᴄᴇ ᴡᴏɴᴅᴇʀʟᴀɴᴅ", "ᴏɴʟɪɴᴇ", "", 0, false));
        items.add(new ChatsFragment.ChatItem("ʙᴏʙ ʙᴜɪʟᴅᴇʀ", "ʟᴀꜱᴛ ꜱᴇᴇɴ ʀᴇᴄᴇɴᴛʟʏ", "", 0, false));
        items.add(new ChatsFragment.ChatItem("ᴄʜᴀʀʟɪᴇ ᴅᴇᴠ", "ᴏɴʟɪɴᴇ", "", 0, false));
        items.add(new ChatsFragment.ChatItem("ᴅᴀᴠɪᴅ ꜱᴍɪᴛʜ", "ʟᴀꜱᴛ ꜱᴇᴇɴ ʏᴇꜱᴛᴇʀᴅᴀʏ", "", 0, false));
        return items;
    }
}
