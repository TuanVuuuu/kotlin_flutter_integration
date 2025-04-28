package com.example.androidapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import com.example.androidapp.databinding.FragmentFlutterBinding

class FlutterFragment : Fragment() {
    private val TAG = "FlutterFragment"
    private val ENGINE_ID = "scenario_engine"
    
    // ViewBinding
    private var _binding: FragmentFlutterBinding? = null
    private val binding get() = _binding!!
    
    // FlutterFragment instance
    private var flutterFragment: FlutterFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFlutterEngine()
    }

    private fun setupFlutterEngine() {
        // Tạo FlutterEngine mới nếu chưa có trong cache
        if (FlutterEngineCache.getInstance().get(ENGINE_ID) == null) {
            val flutterEngine = FlutterEngine(requireContext()).apply {
                navigationChannel.setInitialRoute("scenario_view")
                dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
            }
            FlutterEngineCache.getInstance().put(ENGINE_ID, flutterEngine)
            Log.d(TAG, "Created and cached new FlutterEngine")
        } else {
            Log.d(TAG, "Using existing cached FlutterEngine")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        _binding = FragmentFlutterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")
        
        // Tạo FlutterFragment với engine đã cache
        flutterFragment = FlutterFragment
            .withCachedEngine(ENGINE_ID)
            .shouldAttachEngineToActivity(true)
            .build<FlutterFragment>()

        // Thêm FlutterFragment vào container
        childFragmentManager
            .beginTransaction()
            .replace(binding.flutterContainer.id, flutterFragment!!)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        
        // Xóa FlutterFragment trước khi hủy binding
        flutterFragment?.let {
            childFragmentManager.beginTransaction()
                .remove(it)
                .commit()
            flutterFragment = null
        }
        
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
        
        // Clean up engine if this is the last fragment using it
        if (activity?.supportFragmentManager?.fragments?.none { it is FlutterFragment } == true) {
            FlutterEngineCache.getInstance().get(ENGINE_ID)?.destroy()
            FlutterEngineCache.getInstance().remove(ENGINE_ID)
            Log.d(TAG, "Destroyed FlutterEngine")
        }
    }
} 