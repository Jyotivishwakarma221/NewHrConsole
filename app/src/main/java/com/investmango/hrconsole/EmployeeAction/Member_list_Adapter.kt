package com.investmango.hrconsole.EmployeeAction

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.investmango.hrconsole.EmployeeAction.EmplyDetail.TimeSlotViewHolder
import com.investmango.hrconsole.R
import com.investmango.hrconsole.commonclasses.InitialAvatarView
import com.investmango.hrconsole.manager.activity.NewMessageFragment
import com.investmango.hrconsole.manager.activity.fragment.NewTaskFragment
import com.investmango.hrconsole.model.AssignedUsersItem
import com.investmango.hrconsole.model.TotalEmpResponseItem

class Member_list_Adapter : RecyclerView.Adapter<MemberItem> {
    private var frag: CreateNewFragment? = null
    private var frag2: NewMessageFragment? = null
    private var frag3: AssignTask? = null
    private var memberList: List<TotalEmpResponseItem?> = arrayListOf()
    private lateinit var assigned: List<AssignedUsersItem?>

    // Primary constructor
    constructor(assigned: List<AssignedUsersItem?>) {
        this.assigned = assigned
    }

    // Secondary constructor with a click listener
    constructor(
        frag: CreateNewFragment,
        memberList: List<TotalEmpResponseItem?>,
    ) {
        this.frag = frag
        this.memberList = memberList
    }

    constructor(
        frag: NewMessageFragment,
        memberList: List<TotalEmpResponseItem?>,
    ) {
        this.frag2 = frag
        this.memberList = memberList
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberItem {
        val view = LayoutInflater.from(parent.context)
            .inflate(com.investmango.hrconsole.R.layout.member_layout, parent, false)
        return MemberItem(view)
    }

    override fun getItemCount(): Int {
        if (!memberList.isEmpty())
            return memberList.size
        else if (!assigned.isEmpty())
            return assigned.size
        return 0
    }

    override fun onBindViewHolder(holder: MemberItem, position: Int) {
        if (memberList != null && !memberList.isEmpty()) {
            holder.delete.visibility = View.GONE
//        Glide.with(frag).load(memberList.get(position).)
            holder.name.setText(memberList.get(position)?.userName.toString())

            if (memberList.get(position)?.profile!="" && memberList.get(position)?.profile!=null){

               Glide.with(holder.itemView.context).load(memberList[position]?.profile).into(holder.profilePhoto)
                holder.profilePhoto.visibility=View.VISIBLE
                holder.initialAvatar.visibility=View.GONE
            }else{
                holder.initialAvatar.setName(memberList.get(position)?.userName!!)
                holder.profilePhoto.visibility=View.GONE
                holder.initialAvatar.visibility=View.VISIBLE
            }
            //check from list if it is already selected if it is then turn bg in blue
            holder.memberlayout.setOnClickListener {

                if (memberList.get(position)?.isSelected == false) {

                    memberList.get(position)!!.isSelected=true

                    holder.memberlayout.setBackgroundResource(R.drawable.blue_bg)
                    if (frag != null && frag?.isVisible == true) {
                        if (frag?.editing != true) {
                            memberList.get(position)?.let { it1 ->
                                frag?.selectedList?.add(it1)
                            }
                        } else {
                            memberList.get(position)?.let { it1 ->
                                frag?.assignedUser?.add(
                                    AssignedUsersItem(
                                        it1.userPhone,
                                        it1.id,
                                        false,
                                        it1.userName,
                                        "",
                                        it1.profile

                                    )
                                )
                            }
                        }
                    } else if (frag2 != null && frag2?.isVisible == true)
                        memberList.get(position)?.let { it1 ->
                            frag2?.selectedList?.add(it1)
                        }
                }else{
                    memberList.get(position)!!.isSelected=false
                    holder.memberlayout.setBackgroundResource(R.drawable.dark_grey_bg)
                    if (frag != null && frag?.isVisible == true) {
                        if (frag?.editing != true) {
                            memberList.get(position)?.let { it1 ->
                                frag?.selectedList?.remove(it1)
                            }
                        } else {
                            memberList.get(position)?.let { it1 ->
                                frag?.assignedUser?.remove(
                                    AssignedUsersItem(
                                        it1.userPhone,
                                        it1.id,
                                        false,
                                        it1.userName,
                                        ""
                                    )
                                )
                            }
                        }
                    } else if (frag2 != null && frag2?.isVisible == true)
                        memberList.get(position)?.let { it1 ->
                            frag2?.selectedList?.remove(it1)
                        }
                }
                }
            } else if (assigned != null && !assigned.isEmpty()) {
                Log.e("assigned", "onBindViewHolder: " + assigned)
                holder.name.setText(assigned.get(position)?.assignToName.toString())
            if (assigned[position]?.assignByProfile!=null && assigned[position]?.assignByProfile!=""){
                Glide.with(frag?.context!!).load(assigned.get(position)?.assignByProfile).into(holder.profilePhoto)
                holder.profilePhoto.visibility=View.VISIBLE
                holder.initialAvatar.visibility=View.GONE
            }else{
                holder.initialAvatar.setName(assigned.get(position)?.assignToName.toString())
                holder.profilePhoto.visibility=View.GONE
                holder.initialAvatar.visibility=View.VISIBLE
            }
            }

    }

}

class MemberItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val name: TextView = itemView.findViewById(com.investmango.hrconsole.R.id.nameOfMember)
    val memberlayout: LinearLayout = itemView.findViewById(com.investmango.hrconsole.R.id.memberLay)
    val profilePhoto: ImageView = itemView.findViewById(com.investmango.hrconsole.R.id.profilephoto)
    val initialAvatar: InitialAvatarView = itemView.findViewById(com.investmango.hrconsole.R.id.initialAvatar)
    val delete: ImageView = itemView.findViewById(com.investmango.hrconsole.R.id.delete)

}