package com.nemodream.bangkkujaengi.customer.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nemodream.bangkkujaengi.customer.data.model.Member
import com.nemodream.bangkkujaengi.databinding.RowSocialFollowingAllBinding
import com.nemodream.bangkkujaengi.utils.loadImage

class SocialFollowingAllAdapter(
    private val currentUserDocId: String,
    private val firestore: FirebaseFirestore
) : RecyclerView.Adapter<SocialFollowingAllAdapter.SocialFollowingAllViewHolder>() {
    private val items = mutableListOf<Member>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialFollowingAllViewHolder {
        val binding = RowSocialFollowingAllBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SocialFollowingAllViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SocialFollowingAllViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun submitList(socialFollowingAllRows: List<Member>) {
        items.clear()
        items.addAll(socialFollowingAllRows)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class SocialFollowingAllViewHolder(private val binding: RowSocialFollowingAllBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var isFollowing = true

        fun bind(member: Member) {
            binding.ivProfileImage.loadImage(member.memberProfileImage.toString())
            binding.tvProfileNickname.text = member.memberNickName

            // 초기 상태 설정 (isFollowing 여부에 따라)
            updateButtonState(isFollowing)

            // 버튼 클릭 리스너
            binding.btnFollowingAllFollowing.setOnClickListener {
                isFollowing = !isFollowing
                updateButtonState(isFollowing)

                if (isFollowing) {
                    // 팔로우 동작 호출
                    followMember(member.id)
                } else {
                    // 언팔로우 동작 호출
                    unfollowMember(member.id)
                }
            }
        }

        private fun updateButtonState(isFollowing: Boolean) {
            if (isFollowing) {
                binding.btnFollowingAllFollowing.text = "팔로잉"
                binding.btnFollowingAllFollowing.setTextColor(Color.parseColor("#58443B")) // 글자색
                binding.btnFollowingAllFollowing.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#DFDFDF")) // 버튼 배경색
                )
                binding.btnFollowingAllFollowing.strokeColor =
                    ColorStateList.valueOf(Color.parseColor("#818080")) // 테두리 색상
                binding.btnFollowingAllFollowing.strokeWidth = 1 // 테두리 두께 (px 단위)
            } else {
                binding.btnFollowingAllFollowing.text = "팔로우"
                binding.btnFollowingAllFollowing.setTextColor(Color.parseColor("#FFFFFF")) // 글자색
                binding.btnFollowingAllFollowing.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#332828")) // 버튼 배경색
                )
                binding.btnFollowingAllFollowing.strokeColor =
                    ColorStateList.valueOf(Color.parseColor("#000000")) // 테두리 색상 (기본값, 변경 가능)
                binding.btnFollowingAllFollowing.strokeWidth = 1 // 테두리 두께 (px 단위)
            }
        }

        private fun followMember(memberId: String) {
            val memberRef = firestore.collection("Member").document(memberId)
            val currentUserRef = firestore.collection("Member").document(currentUserDocId)

            firestore.runBatch { batch ->
                // 현재 유저의 followingCount 증가 및 followingList 업데이트
                batch.update(
                    currentUserRef,
                    mapOf(
                        "followingCount" to FieldValue.increment(1),
                        "followingList" to FieldValue.arrayUnion(memberRef)
                    )
                )

                // 상대방 유저의 followerCount 증가
                batch.update(
                    memberRef,
                    "followerCount",
                    FieldValue.increment(1)
                )
            }.addOnFailureListener { e ->
                e.printStackTrace()
            }
        }

        private fun unfollowMember(memberId: String) {
            val memberRef = firestore.collection("Member").document(memberId)
            val currentUserRef = firestore.collection("Member").document(currentUserDocId)

            firestore.runBatch { batch ->
                // 현재 유저의 followingCount 감소 및 followingList 업데이트
                batch.update(
                    currentUserRef,
                    mapOf(
                        "followingCount" to FieldValue.increment(-1),
                        "followingList" to FieldValue.arrayRemove(memberRef)
                    )
                )

                // 상대방 유저의 followerCount 감소
                batch.update(
                    memberRef,
                    "followerCount",
                    FieldValue.increment(-1)
                )
            }.addOnFailureListener { e ->
                e.printStackTrace()
            }
        }
    }
}