package com.example.taskmanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.models.Comment;
import com.example.taskmanager.utils.DateTimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUsername, tvCommentContent, tvCommentTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            // Sửa ID từ tv_username thành tv_user_name để khớp với layout
            tvUsername = itemView.findViewById(R.id.tv_user_name);
            tvCommentContent = itemView.findViewById(R.id.tv_comment_content);
            tvCommentTime = itemView.findViewById(R.id.tv_comment_time);
        }

        // Trong phương thức bind của CommentViewHolder
        public void bind(Comment comment) {
            // Kiểm tra null cho TextView và Comment
            if (tvUsername != null) {
                // Hiển thị người bình luận
                if (comment.getUser() != null) {
                    tvUsername.setText(comment.getUser().getFullName());
                } else {
                    tvUsername.setText(R.string.unknown_user);
                }
            }

            // Kiểm tra null cho tvCommentContent
            if (tvCommentContent != null && comment.getContent() != null) {
                // Hiển thị nội dung bình luận
                tvCommentContent.setText(comment.getContent());
            }

            // Kiểm tra null cho tvCommentTime
            if (tvCommentTime != null && comment.getCreatedAt() != null) {
                // Định dạng và hiển thị thời gian bình luận
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date commentDate = inputFormat.parse(comment.getCreatedAt());

                    // Định dạng hiển thị
                    String formattedDate = getRelativeTimeSpan(commentDate);
                    tvCommentTime.setText(formattedDate);
                } catch (ParseException e) {
                    tvCommentTime.setText(comment.getCreatedAt());
                } catch (Exception e) {
                    // Xử lý các ngoại lệ khác
                    tvCommentTime.setText("");
                    e.printStackTrace();
                }
            }
        }

        private String getRelativeTimeSpan(Date date) {
            if (date == null) {
                return "";
            }

            try {
                long currentTime = System.currentTimeMillis();
                long commentTime = date.getTime();
                long diffTime = currentTime - commentTime;

                // Chuyển đổi sang giây, phút, giờ, ngày
                long seconds = diffTime / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;

                if (days > 0) {
                    if (days == 1) {
                        return context.getString(R.string.yesterday);
                    } else if (days < 7) {
                        return days + " " + context.getString(R.string.days_ago);
                    } else {
                        return DateTimeUtils.formatDisplayDate(DateTimeUtils.formatDate(date));
                    }
                } else if (hours > 0) {
                    return hours + " " + context.getString(R.string.hours_ago);
                } else if (minutes > 0) {
                    return minutes + " " + context.getString(R.string.minutes_ago);
                } else {
                    return context.getString(R.string.just_now);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
    }
}