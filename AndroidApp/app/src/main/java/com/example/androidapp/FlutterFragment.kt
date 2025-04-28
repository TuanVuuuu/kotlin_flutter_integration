package com.example.androidapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import io.flutter.embedding.android.FlutterView
import com.example.androidapp.databinding.FragmentFlutterBinding

class FlutterFragment : Fragment() {
    private var redFlutterView: FlutterView? = null
    private var blueFlutterView: FlutterView? = null
    
    // ViewBinding
    private var _binding: FragmentFlutterBinding? = null
    private val binding get() = _binding!!
    
    // Sử dụng FlutterViewManager để quản lý Flutter views
    private lateinit var flutterViewManager: FlutterViewManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Khởi tạo ViewBinding
        _binding = FragmentFlutterBinding.inflate(inflater, container, false)
        val view = binding.root
        
        // Khởi tạo FlutterViewManager
        flutterViewManager = FlutterViewManager.getInstance(requireContext())
        
        // Thiết lập Flutter Views
        setupFlutterViews()

        return view
    }

    private fun setupFlutterViews() {
        // Sử dụng binding để tham chiếu đến views
        redFlutterView = flutterViewManager.attachRedFlutterView(binding.redSquareContainer)
        blueFlutterView = flutterViewManager.attachBlueFlutterView(binding.blueSquareContainer)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Tách FlutterView khỏi engine khi fragment bị hủy
        redFlutterView?.detachFromFlutterEngine()
        blueFlutterView?.detachFromFlutterEngine()
        
        // Xóa binding khi view bị hủy để tránh memory leak
        _binding = null
    }
} 