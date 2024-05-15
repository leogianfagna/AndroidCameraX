package me.leogianfagna.camerax

import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import me.leogianfagna.camerax.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar o layout = Rechear o layout com as views
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // Inflar a partir da raíz (LinearLayoutCompat)

        binding.btnOpenCamera.setOnClickListener {

            // Solicitar permissão da câmera
            cameraProviderResult.launch(android.Manifest.permission.CAMERA)
        }
    }

    private val cameraProviderResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                abrirTelaDePreview()
            } else {
                Snackbar.make(binding.root, "Você não concedeu permissões para usar a câmera", Snackbar.LENGTH_INDEFINITE).show()

            }
        }

    private fun abrirTelaDePreview() {
        val i = Intent(this, CameraPreviewActivity::class.java)
        startActivity(i)
    }
}