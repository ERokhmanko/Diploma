package ru.netology.diploma.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.diploma.R
import ru.netology.diploma.adapter.EventCallback
import ru.netology.diploma.adapter.EventsAdapter
import ru.netology.diploma.adapter.PostLoadStateAdapter
import ru.netology.diploma.databinding.FragmentEventsBinding
import ru.netology.diploma.dto.Event
import ru.netology.diploma.viewmodel.EventViewModel
import ru.netology.diploma.viewmodel.UserViewModel

@AndroidEntryPoint
class EventsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentEventsBinding.inflate(inflater, container, false)
        val bundle = Bundle()

        val eventViewModel: EventViewModel by activityViewModels()
        val userViewModel: UserViewModel by activityViewModels()

        val adapter = EventsAdapter(object : EventCallback {
            override fun onLike(event: Event) {
                if (!event.likedByMe) eventViewModel.likeById(event.id)
                else eventViewModel.unlikeById(event.id)
            }

            override fun onShare(event: Event) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, event.content)
                }

                val shareIntent = Intent.createChooser(intent, getString(R.string.share_post))
                startActivity(shareIntent)
            }

            override fun onJoin(event: Event) {
                if (!event.participatedByMe) eventViewModel.participateById(event.id)
                else eventViewModel.unParticipateById(event.id)
            }

            override fun remove(event: Event) {
                eventViewModel.removeById(event.id)
            }

            override fun edit(event: Event) {
                eventViewModel.edit(event)
                bundle.apply {
                    putString("dateTime", event.datetime)
                    putString("content", event.content)
                    putString("format", event.type.toString())
                    putString("link", event.link)
                    event.coords?.lat?.let { putDouble("lat", it) }
                    event.coords?.long?.let { putDouble("lng", it) }
                    putString("attachment", event.attachment?.url)
                    putString("attachmentType", event.attachment?.type?.name.toString())

                }

                findNavController().navigate(
                    R.id.action_navigation_events_to_newEventFragment,
                    bundle
                )
            }

            override fun hide(event: Event) {
                //Этот метод сделан для примера различного меню у постов и не имеет корректной реализации.
                eventViewModel.hideEvent(event)
            }

            override fun onVideo(event: Event) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.attachment?.url))
                val videoIntent = Intent.createChooser(intent, getString(R.string.media_chooser))
                startActivity(videoIntent)
            }

            override fun onAudio(event: Event) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.attachment?.url))
                val audioIntent = Intent.createChooser(intent, getString(R.string.media_chooser))
                startActivity(audioIntent)
            }

            override fun onLink(event: Event) {
                val uri =
                    if (event.link?.contains("http://") == true) event.link else "http://${event.link}"

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

                startActivity(intent)
            }

            override fun onSpeakers(event: Event) {
                userViewModel.getUsersIds(event.speakerIds)
                findNavController().navigate(R.id.action_navigation_events_to_usersBottomSheet)
            }

            override fun onLikeOwner(event: Event) {
                userViewModel.getUsersIds(event.likeOwnerIds)
                findNavController().navigate(R.id.action_navigation_events_to_usersBottomSheet)
            }

            override fun onParticipants(event: Event) {
                userViewModel.getUsersIds(event.participantsIds)
                findNavController().navigate(R.id.action_navigation_events_to_usersBottomSheet)
            }

            override fun onMap(event: Event) {
                event.coords?.lat?.let { bundle.putDouble("lat", it) }
                event.coords?.long?.let { bundle.putDouble("lng", it) }
                findNavController().navigate(R.id.action_navigation_events_to_mapFragment, bundle)
            }
        })


        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadStateAdapter {
                adapter.retry()
            },
            footer = PostLoadStateAdapter {
                adapter.retry()
            })

        binding.swipeRefresh.setOnRefreshListener(adapter::refresh)

        eventViewModel.dataState.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launchWhenCreated {
            eventViewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeRefresh.isRefreshing =
                    state.refresh is LoadState.Loading ||
                            state.prepend is LoadState.Loading ||
                            state.append is LoadState.Loading
            }
        }


        return binding.root
    }
}
