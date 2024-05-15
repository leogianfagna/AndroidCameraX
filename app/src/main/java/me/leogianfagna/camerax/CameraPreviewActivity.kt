package me.leogianfagna.camerax

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.common.util.concurrent.ListenableFuture
import me.leogianfagna.camerax.databinding.ActivityCameraPreviewBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraPreviewBinding

    // Controla as instâncias PROVIDER, não deixa abrir mais de uma tela de permissão
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    // Selecionar qual câmera iremos trabalhar
    private lateinit var cameraSelector: CameraSelector

    // Imagem capturada, já nasce como nula
    private var imageCapture: ImageCapture? = null

    // Objeto do android que cria uma thread para gravar a imagem (não podemos usar a mesma thread
    // para isso, pois já estaremos usando-a para o preview)
    private lateinit var imgCaptureExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
            blinkPreview()
        }
    }

    private fun startCamera() {

        imageCapture = ImageCapture.Builder().build()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            try {
                // Abrir o preview
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture)
            } catch (e: Exception) {
                Log.e("Camera Preview", "Falha ao abrir a camera")

            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        imageCapture?.let {

            // Nome do arquivo para gravar a foto
            val fileName = "JPEG_${System.currentTimeMillis()}"
            val file = File(externalMediaDirs[0], fileName)
            // externalMediaDirs é um vetor que tem todos os diretórios de mídia do cel
            // File é um objeto da biblioteca File
            // Por ser [0], vai salvar na memória interna

            // Preparando o arquivo para a saída
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {

                    // Os dois métodos que OnImageSavedCallback EXIGE ter
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.i("Camera Preview", "A imagem foi salva no diretório: ${file.toURI()}")
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(binding.root.context, "Erro ao salvar foto", Toast.LENGTH_LONG).show()
                        Log.e("Camera Preview", "Exceção ao gravar arquivo: $exception")
                    }

                }
            )

        }
    }

    private fun blinkPreview() {
        // Executa depois de um certo tempo
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)

            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)

        }, 100)
    }
}
