package ru.geekbrains.HW3;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.geekbrains.HW3.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnStart.setOnClickListener(v -> {
            binding.pbProgressBar.setVisibility(View.VISIBLE);
            convertPicture();
            showCancelDialog();
        });
    }

    private void convertPicture() {
        Disposable converter =
                Single.create((SingleOnSubscribe<File>) emitter -> {
                    try {
                        File newPic = convertToPng();
                        emitter.onSuccess(newPic);
                    } catch (IOException e) {
                        emitter.onError(e);
                    }
                })
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onResult);
        compositeDisposable.add(converter);
    }

    private void onResult(File file, Throwable throwable) {
        binding.pbProgressBar.setVisibility(View.GONE);
        alertDialog.dismiss();
        if (throwable != null) {
            Toast.makeText(MainActivity.this,
                    throwable.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this,
                    file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }

    private File convertToPng() throws IOException {
        FileOutputStream fileOutputStream = null;
        File dirPath = MainActivity.this.getCacheDir();
        File newPic = new File(dirPath, "newPic.png");
        try {
            InputStream imageStream = MainActivity.this.getResources().openRawResource(R.raw.pic);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            fileOutputStream = new FileOutputStream(newPic);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
        return newPic;
    }
    private void showCancelDialog() {
        if (alertDialog == null || !alertDialog.isShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Выполняется конвертация");
            builder.setPositiveButton(R.string.cancel, (dialog, which) -> {
                dialog.dismiss();
                compositeDisposable.clear();
                binding.pbProgressBar.setVisibility(View.GONE);
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
    }


}