package com.example.taskmanager.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.adapters.CommentAdapter;
import com.example.taskmanager.database.CommentDAO;
import com.example.taskmanager.database.TaskDAO;
import com.example.taskmanager.models.Comment;
import com.example.taskmanager.models.Task;
import com.example.taskmanager.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class TaskCommentsFragment extends Fragment {

    private RecyclerView recyclerViewComments;
    private TextView tvNoComments;
    private EditText etCommentContent;
    private Button btnSendComment;
    private ProgressBar progressBar;

    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private CommentDAO commentDAO;
    private TaskDAO taskDAO;
    private SessionManager sessionManager;

    private long taskId;
    private Task task;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_comments, container, false);

        // Khởi tạo các thành phần giao diện
        initViews(view);

        // Khởi tạo database và session
        commentDAO = new CommentDAO(getContext());
        taskDAO = new TaskDAO(getContext());
        sessionManager = new SessionManager(getContext());

        // Lấy ID nhiệm vụ từ arguments
        Bundle args = getArguments();
        if (args != null) {
            taskId = args.getLong("TASK_ID", -1);
        }

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập sự kiện click
        setupClickListeners();

        // Tải dữ liệu
        loadData();

        return view;
    }

    private void initViews(View view) {
        recyclerViewComments = view.findViewById(R.id.recycler_view_comments);
        tvNoComments = view.findViewById(R.id.tv_no_comments);
        etCommentContent = view.findViewById(R.id.et_comment_content);
        btnSendComment = view.findViewById(R.id.btn_send_comment);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(getContext(), commentList);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewComments.setAdapter(commentAdapter);
    }

    private void setupClickListeners() {
        // Sự kiện khi nhấn nút gửi bình luận
        btnSendComment.setOnClickListener(v -> {
            String content = etCommentContent.getText().toString().trim();
            if (!content.isEmpty()) {
                addComment(content);
            } else {
                Toast.makeText(getContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Sửa phương thức loadData() trong TaskCommentsFragment
    private void loadData() {
        showProgress(true);

        new Thread(() -> {
            CommentDAO localCommentDAO = null;
            try {
                // Tạo DAO mới cho thread này
                localCommentDAO = new CommentDAO(getContext());
                localCommentDAO.open();

                // Lấy danh sách bình luận
                final List<Comment> comments = localCommentDAO.getCommentsByTaskId(taskId);

                // Cập nhật UI trên main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            showProgress(false);

                            commentList.clear();
                            if (comments != null) {
                                commentList.addAll(comments);
                            }
                            commentAdapter.notifyDataSetChanged();

                            // Cập nhật giao diện
                            updateEmptyView();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    });
                }
            } finally {
                // Đảm bảo đóng connection trong cùng thread
                if (localCommentDAO != null) {
                    localCommentDAO.close();
                }
            }
        }).start();
    }
    private void addComment(String content) {
        showProgress(true);

        new Thread(() -> {
            // Tạo comment mới
            Comment comment = new Comment(taskId, sessionManager.getUserId(), content);

            // Lưu vào database
            commentDAO.open();
            long commentId = commentDAO.createComment(comment);
            commentDAO.close();

            // Cập nhật UI trên main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    showProgress(false);

                    if (commentId > 0) {
                        // Thêm comment vào danh sách
                        commentDAO.open();
                        Comment newComment = commentDAO.getCommentById(commentId);
                        commentDAO.close();

                        if (newComment != null) {
                            commentList.add(newComment);
                            commentAdapter.notifyDataSetChanged();

                            // Cuộn xuống dưới cùng
                            recyclerViewComments.smoothScrollToPosition(commentList.size() - 1);

                            // Xóa nội dung input
                            etCommentContent.setText("");

                            // Cập nhật hiển thị khi có comment mới
                            updateEmptyView();
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private void updateEmptyView() {
        if (commentList.isEmpty()) {
            tvNoComments.setVisibility(View.VISIBLE);
            recyclerViewComments.setVisibility(View.GONE);
        } else {
            tvNoComments.setVisibility(View.GONE);
            recyclerViewComments.setVisibility(View.VISIBLE);
        }
    }

    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi quay lại fragment
        if (taskId > 0) {
            loadData();
        }
    }
}