package game.is.life.videofilter.adapter;

/**
 * Created by yzhao on 5/30/17.
 */

public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}
