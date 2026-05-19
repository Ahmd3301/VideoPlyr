package io.videoplyr.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.videoplyr.app.databinding.ItemPlaylistBinding

class PlaylistAdapter(
    private val items: List<PlaylistItem>,
    private val rootViewGroup: ViewGroup,
    private val onClick: (PlaylistItem, Int) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.VH>() {

    private var activeIndex = 0

    inner class VH(val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        b.cardBlur.setupWith(rootViewGroup).setBlurRadius(4f).setBlurAutoUpdate(true)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.itemTitle.text = items[position].title
        holder.binding.cardBlur.setOverlayColor(
            if (position == activeIndex) 0x40FFFFFF.toInt() else 0x1FFFFFFF.toInt()
        )
        holder.itemView.setOnClickListener {
            val prev = activeIndex; activeIndex = position
            notifyItemChanged(prev); notifyItemChanged(position)
            onClick(items[position], position)
        }
    }

    override fun getItemCount() = items.size
    fun setActive(i: Int) { val p = activeIndex; activeIndex = i; notifyItemChanged(p); notifyItemChanged(i) }
}
