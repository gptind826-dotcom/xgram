/*
 * ᴍᴀɪɴᴘᴀɢᴇʀᴀᴅᴀᴘᴛᴇʀ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ᴠɪᴇᴡᴘᴀɢᴇʀ2 ᴀᴅᴀᴘᴛᴇʀ ꜰᴏʀ ᴍᴀɪɴ ᴛᴀʙꜱ
 */

package com.alternative.telegram;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 4;

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ChatsFragment.newInstance();
            case 1:
                return ChannelsFragment.newInstance();
            case 2:
                return GroupsFragment.newInstance();
            case 3:
                return ContactsFragment.newInstance();
            default:
                return ChatsFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
