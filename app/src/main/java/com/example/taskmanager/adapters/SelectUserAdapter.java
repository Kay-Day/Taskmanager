package com.example.taskmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectUserAdapter extends RecyclerView.Adapter<SelectUserAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private OnUserClickListener listener;
    private Map<Long, Boolean> selectedUsers = new HashMap<>();

    public interface OnUserClickListener {
        void onUserClick(User user, boolean isSelected);
    }

    public SelectUserAdapter(Context context, List<User> userList, OnUserClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_select_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bindData(user);
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvFullName, tvEmail;
        CheckBox checkBox;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvFullName = itemView.findViewById(R.id.tv_full_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            checkBox = itemView.findViewById(R.id.checkbox);

            // Xử lý sự kiện khi click vào item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    User user = userList.get(position);
                    boolean newState = !checkBox.isChecked();
                    checkBox.setChecked(newState);
                    selectedUsers.put(user.getId(), newState);
                    if (listener != null) {
                        listener.onUserClick(user, newState);
                    }
                }
            });

            // Xử lý sự kiện khi click vào checkbox
            checkBox.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    User user = userList.get(position);
                    boolean isChecked = checkBox.isChecked();
                    selectedUsers.put(user.getId(), isChecked);
                    if (listener != null) {
                        listener.onUserClick(user, isChecked);
                    }
                }
            });
        }

        public void bindData(User user) {
            tvUsername.setText(user.getUsername());
            tvFullName.setText(user.getFullName());
            tvEmail.setText(user.getEmail());

            // Hiển thị trạng thái đã chọn
            boolean isSelected = selectedUsers.containsKey(user.getId()) && selectedUsers.get(user.getId());
            checkBox.setChecked(isSelected);
        }
    }
}