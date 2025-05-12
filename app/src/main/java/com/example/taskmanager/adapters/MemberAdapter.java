package com.example.taskmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.models.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private Context context;
    private List<User> memberList;
    private OnMemberActionListener listener;

    // Interface cho sự kiện xóa thành viên
    public interface OnMemberActionListener {
        void onRemoveMember(User member);
    }

    public MemberAdapter(Context context, List<User> memberList, OnMemberActionListener listener) {
        this.context = context;
        this.memberList = memberList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User member = memberList.get(position);
        holder.bind(member);
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgUserAvatar;
        private TextView tvMemberName, tvMemberEmail;
        private Button btnRemoveMember;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUserAvatar = itemView.findViewById(R.id.img_user_avatar);
            tvMemberName = itemView.findViewById(R.id.tv_member_name);
            tvMemberEmail = itemView.findViewById(R.id.tv_member_email);
            btnRemoveMember = itemView.findViewById(R.id.btn_remove_member);
        }

        public void bind(User member) {
            // Hiển thị thông tin thành viên
            tvMemberName.setText(member.getFullName());
            tvMemberEmail.setText(member.getEmail());

            // Sự kiện khi nhấn nút xóa thành viên
            btnRemoveMember.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveMember(member);
                }
            });
        }
    }
}