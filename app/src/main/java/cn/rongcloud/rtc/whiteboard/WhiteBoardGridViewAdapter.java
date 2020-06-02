package cn.rongcloud.rtc.whiteboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.rongcloud.rtc.R;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.domain.Appliance;
import com.herewhite.sdk.domain.MemberState;

public class WhiteBoardGridViewAdapter extends BaseAdapter {
    private Context context;
    private Room room;
    private PencilColorPopupWindow pencilColorPopupWindow;
    private static int[] PENCIL_COLORS = {
        R.color.white_board_pencil_color_red,
        R.color.white_board_pencil_color_orange,
        R.color.white_board_pencil_color_yellow,
        R.color.white_board_pencil_color_blue,
        R.color.white_board_pencil_color_cyan,
        R.color.white_board_pencil_color_green,
        R.color.white_board_pencil_color_black,
        R.color.white_board_pencil_color_purple,
        R.color.white_board_pencil_color_gray
    };

    public WhiteBoardGridViewAdapter(
            Context context, Room room, PencilColorPopupWindow pencilColorPopupWindow) {
        this.context = context;
        this.room = room;
        this.pencilColorPopupWindow = pencilColorPopupWindow;
    }

    @Override
    public int getCount() {
        return PENCIL_COLORS.length;
    }

    @Override
    public Object getItem(int position) {
        return PENCIL_COLORS[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView =
                    LayoutInflater.from(context)
                            .inflate(R.layout.item_white_board_pencil_grid_view_item, null);
        }
        final TextView tvColorItem = (TextView) convertView.findViewById(R.id.tv_pencil_color_item);
        final int color = context.getResources().getColor(PENCIL_COLORS[position]);
        tvColorItem.setBackgroundColor(color);
        tvColorItem.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int red = (color & 0xff0000) >> 16;
                        int green = (color & 0x00ff00) >> 8;
                        int blue = (color & 0x0000ff);
                        MemberState memberState = new MemberState();
                        memberState.setStrokeColor(new int[] {red, green, blue});
                        memberState.setCurrentApplianceName(Appliance.PENCIL);
                        room.setMemberState(memberState);
                        pencilColorPopupWindow.dismiss();
                    }
                });
        return convertView;
    }
}
