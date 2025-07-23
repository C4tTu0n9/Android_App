package com.example.appcore.utils;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.appcore.R;
import com.example.appcore.dao.CategoryDAO;
import com.example.appcore.dao.MovieDAO;
import com.example.appcore.data.models.Actor;
import com.example.appcore.data.models.Category;
import com.example.appcore.data.models.Movie;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Một Fragment đa năng dùng để Thêm mới hoặc Cập nhật thông tin một bộ phim.
 * - Chế độ "Thêm mới": Được khởi tạo bằng newInstance().
 * - Chế độ "Cập nhật": Được khởi tạo bằng newInstance(movie), truyền vào đối tượng phim cần sửa.
 */
public class AddMovieFragment extends BottomSheetDialogFragment {

    // --- Constants ---
    private static final String KEY_MOVIE_TO_UPDATE = "MOVIE_TO_UPDATE";

    // --- Views ---
    private TextView tvDialogTitle;
    private TextInputEditText edtMovieName, edtDirector, edtMovieTime, edtDescription, edtPrice, edtCast;
    private AutoCompleteTextView spinnerCategory, spinnerStatus;
    private ImageView imgPreview;
    private Button btnSelectImage, btnSaveMovie;
    private ProgressBar progressBar;

    // --- Data & DAO ---
    private MovieDAO movieDAO;
    private CategoryDAO categoryDAO;
    private List<Category> categoryList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // --- State ---
    private Uri newImageUri; // Chỉ dùng cho ảnh mới được chọn
    private Movie movieToUpdate;
    private boolean isUpdateMode = false;
    private ValueEventListener categoryValueEventListener;

    // --- Factory Methods ---

    /**
     * Tạo một instance của Fragment cho chế độ THÊM MỚI.
     */
    public static AddMovieFragment newInstance() {
        return new AddMovieFragment();
    }

    /**
     * Tạo một instance của Fragment cho chế độ CẬP NHẬT.
     * @param movie Đối tượng Movie cần cập nhật (phải implement Serializable).
     */
    public static AddMovieFragment newInstance(Movie movie) {
        AddMovieFragment fragment = new AddMovieFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_MOVIE_TO_UPDATE, movie);
        fragment.setArguments(args);
        return fragment;
    }

    // --- Lifecycle Methods ---

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movieToUpdate = (Movie) getArguments().getSerializable(KEY_MOVIE_TO_UPDATE);
            if (movieToUpdate != null) {
                isUpdateMode = true;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_movie, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        movieDAO = new MovieDAO();
        categoryDAO = new CategoryDAO();

        bindViews(view);
        setupInitialUI();
        setupListeners();
        setupStatusSpinner();
        loadCategories(); // Tải categories, sau đó sẽ điền dữ liệu nếu là chế độ update
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (categoryDAO != null && categoryValueEventListener != null) {
            categoryDAO.removeListener(categoryValueEventListener);
        }
    }

    // --- UI Setup Methods ---

    private void bindViews(View view) {
        tvDialogTitle = view.findViewById(R.id.textViewDialogTitle); // Thêm ID này vào layout XML
        edtMovieName = view.findViewById(R.id.edt_movie_name);
        edtDirector = view.findViewById(R.id.edt_director);
        edtMovieTime = view.findViewById(R.id.edt_movie_time);
        edtDescription = view.findViewById(R.id.edt_description);
        edtPrice = view.findViewById(R.id.edt_price);
        edtCast = view.findViewById(R.id.edt_cast);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerStatus = view.findViewById(R.id.spinner_status);
        imgPreview = view.findViewById(R.id.img_preview);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        btnSaveMovie = view.findViewById(R.id.btn_save_movie);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    /**
     * Cài đặt giao diện ban đầu dựa trên chế độ (Thêm mới / Cập nhật).
     */
    private void setupInitialUI() {
        if (isUpdateMode) {
            tvDialogTitle.setText("Cập Nhật Phim");
            btnSaveMovie.setText("Cập Nhật");
        } else {
            tvDialogTitle.setText("Thêm Phim Mới");
            btnSaveMovie.setText("Lưu Phim");
        }
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> mGetContent.launch("image/*"));
        btnSaveMovie.setOnClickListener(v -> validateData());
    }

    private void setupStatusSpinner() {
        String[] statuses = {"now_showing", "upcoming"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses);
        spinnerStatus.setAdapter(statusAdapter);
    }

    /**
     * Điền dữ liệu của phim cần cập nhật vào các trường trong UI.
     */
    private void populateUiForUpdate() {
        if (!isUpdateMode || movieToUpdate == null) return;

        edtMovieName.setText(movieToUpdate.getMovieName());
        edtDirector.setText(movieToUpdate.getDirector());
        edtDescription.setText(movieToUpdate.getDescription());
        edtPrice.setText(String.valueOf(movieToUpdate.getPrice()));
        edtMovieTime.setText(String.valueOf(movieToUpdate.getDurationInMinutes()));


        if (movieToUpdate.getImage() != null && !movieToUpdate.getImage().isEmpty() && getContext() != null) {
            Glide.with(getContext()).load(movieToUpdate.getImage()).into(imgPreview);
        }

        if (movieToUpdate.getCast() != null) {
            StringBuilder castString = new StringBuilder();
            for (int i = 0; i < movieToUpdate.getCast().size(); i++) {
                castString.append(movieToUpdate.getCast().get(i).getActorName());
                if (i < movieToUpdate.getCast().size() - 1) castString.append(", ");
            }
            edtCast.setText(castString.toString());
        }

        // Chọn đúng item trong các spinner
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getCategoryId().equals(movieToUpdate.getCategoryId())) {
                spinnerCategory.setText(categoryList.get(i).getCategoryName(), false);
                break;
            }
        }
        spinnerStatus.setText(movieToUpdate.getStatus(), false);
    }

    // --- Data Handling Methods ---

    private void loadCategories() {
        categoryValueEventListener = categoryDAO.getAllCategories(new CategoryDAO.DataFetchListener() {
            @Override
            public void onDataFetched(List<Category> categories) {
                if (getContext() == null) return;
                categoryList.clear();
                categoryList.addAll(categories);
                List<String> categoryNames = new ArrayList<>();
                for (Category cat : categories) {
                    categoryNames.add(cat.getCategoryName());
                }
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
                spinnerCategory.setAdapter(categoryAdapter);

                // Sau khi đã có danh sách thể loại, mới điền dữ liệu cho chế độ cập nhật
                if (isUpdateMode) {
                    populateUiForUpdate();
                }
            }
            @Override
            public void onError(DatabaseError error) {
                if (getContext() != null) Toast.makeText(getContext(), "Lỗi tải danh sách thể loại!", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi tải thể loại: ", error.toException());
            }
        });
    }

    /**
     * Kiểm tra dữ liệu nhập vào và bắt đầu quá trình lưu.
     */
    private void validateData() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thực hiện!", Toast.LENGTH_LONG).show();
            return;
        }

        // Kiểm tra các trường không được rỗng
        String name = edtMovieName.getText().toString().trim();
        if (TextUtils.isEmpty(name) /* ... thêm các kiểm tra khác nếu cần ... */) {
            Toast.makeText(getContext(), "Tên phim không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu người dùng không chọn ảnh mới khi update, vẫn hợp lệ
        if (!isUpdateMode && newImageUri == null) {
            Toast.makeText(getContext(), "Vui lòng chọn ảnh poster!", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        handleImageAndSave();
    }

    /**
     * Quyết định xem có cần upload ảnh mới hay không trước khi lưu vào database.
     */
    private void handleImageAndSave() {
        // 1. Nếu có ảnh mới được chọn (dù là thêm mới hay cập nhật) -> Upload ảnh mới
        if (newImageUri != null) {
            uploadImageToFirebase(newImageUri);
        }
        // 2. Nếu là chế độ cập nhật và không có ảnh mới được chọn -> Dùng lại ảnh cũ
        else if (isUpdateMode) {
            saveMovieToDatabase(movieToUpdate.getImage());
        }
    }

    private void uploadImageToFirebase(Uri uri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("movie_posters/" + UUID.randomUUID().toString());

        storageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    saveMovieToDatabase(downloadUri.toString());
                }))
                .addOnFailureListener(e -> {
                    if(getContext() != null) Toast.makeText(getContext(), "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
    }

    /**
     * Bước cuối cùng: Lưu đối tượng Movie vào Realtime Database.
     * @param imageUrl URL của ảnh trên Firebase Storage.
     */
    private void saveMovieToDatabase(String imageUrl) {
        try {
            Movie movieData = collectDataFromUi(imageUrl);

            MovieDAO.ActionCallback callback = new MovieDAO.ActionCallback() {
                @Override
                public void onSuccess() {
                    if (getContext() == null) return;
                    String message = isUpdateMode ? "Cập nhật phim thành công!" : "Thêm phim thành công!";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    setLoading(false);
                    dismiss();
                }
                @Override
                public void onFailure(Exception e) {
                    if (getContext() == null) return;
                    String message = isUpdateMode ? "Cập nhật thất bại!" : "Thêm phim thất bại!";
                    Toast.makeText(getContext(), message + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setLoading(false);
                }
            };

            if (isUpdateMode) {
                movieDAO.updateMovie(movieData, callback);
            } else {
                movieDAO.addMovie(movieData, callback);
            }
        } catch (Exception e) {
            if(getContext() != null) Toast.makeText(getContext(), "Lỗi định dạng dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            setLoading(false);
        }
    }

    /**
     * Thu thập toàn bộ dữ liệu từ các trường UI và tạo ra một đối tượng Movie.
     * @param imageUrl URL của ảnh poster.
     * @return một đối tượng Movie hoàn chỉnh.
     * @throws ParseException nếu định dạng ngày không hợp lệ.
     */
    private Movie collectDataFromUi(String imageUrl) throws ParseException {
        String name = edtMovieName.getText().toString().trim();
        String director = edtDirector.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        double price = Double.parseDouble(edtPrice.getText().toString().trim());
        String status = spinnerStatus.getText().toString();

        int durationInMinutes = Integer.parseInt(edtMovieTime.getText().toString().trim());

        String selectedCategoryName = spinnerCategory.getText().toString();
        String categoryId = "";
        for (Category cat : categoryList) {
            if (cat.getCategoryName().equals(selectedCategoryName)) {
                categoryId = cat.getCategoryId();
                break;
            }
        }

        List<Actor> castList = new ArrayList<>();
        String[] actorNames = edtCast.getText().toString().trim().split(",");
        for (String actorName : actorNames) {
            if (!actorName.trim().isEmpty()) {
                castList.add(new Actor(null, actorName.trim())); // ID diễn viên có thể tự sinh sau
            }
        }

        // Dùng lại ID cũ nếu là chế độ cập nhật
        String movieId = isUpdateMode ? movieToUpdate.getMovieId() : null;

        return new Movie(movieId, name, director, description, imageUrl,
                categoryId, price, 0,  castList, status,durationInMinutes);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveMovie.setEnabled(!isLoading);
    }

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    newImageUri = uri; // Lưu vào biến ảnh mới
                    if (getContext() != null) {
                        Glide.with(getContext()).load(newImageUri).into(imgPreview);
                    }
                }
            });
}