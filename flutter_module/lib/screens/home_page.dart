import 'package:flutter/material.dart';
import 'package:get/get.dart';
import '../controllers/counter_controller.dart';

/// Home page view
class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final CounterController controller = Get.find<CounterController>();
    
    return Scaffold(
      appBar: AppBar(
        title: const Text('Flutter Module Home'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'Đây là màn hình Flutter mặc định',
              style: TextStyle(fontSize: 16),
            ),
            const SizedBox(height: 20),
            _buildCounterDisplay(controller),
            _buildMessageDisplay(controller),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: controller.increment,
        tooltip: 'Increment',
        child: const Icon(Icons.add),
      ),
    );
  }
  
  /// Build the counter display widget
  Widget _buildCounterDisplay(CounterController controller) {
    return Obx(() => Text(
      'Counter: ${controller.count}',
      style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
    ));
  }
  
  /// Build the message display widget
  Widget _buildMessageDisplay(CounterController controller) {
    return Obx(() => Visibility(
      visible: controller.hasMessage(),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Text(
          'Message từ Native: ${controller.message}',
          style: const TextStyle(
            color: Colors.blue,
            fontWeight: FontWeight.bold,
            fontSize: 16,
          ),
        ),
      ),
    ));
  }
} 