import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_module/managers/tutorial_manager.dart';

class ScenarioView extends StatefulWidget {
  const ScenarioView({Key? key}) : super(key: key);

  @override
  State<ScenarioView> createState() => _ScenarioViewState();
}

class _ScenarioViewState extends State<ScenarioView> {
  final _scaffoldKey = GlobalKey<ScaffoldState>();
  final _methodChannel = const MethodChannel('flutter_module_channel');
  
  // Keys for tutorial steps
  final _button1Key = GlobalKey();
  final _button2Key = GlobalKey();
  final _button3Key = GlobalKey();
  final _button4Key = GlobalKey();
  final _listKey = GlobalKey();
  final _firstItemKey = GlobalKey();
  final _lastItemKey = GlobalKey();
  // Tutorial manager instance
  TutorialManager? _tutorialManager;

  @override
  void initState() {
    super.initState();
    _setupMethodChannel();
    _setupTutorial();
  }

  @override
  void dispose() {
    _tutorialManager?.cancelTutorial();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(
        title: const Text('Scenario View'),
        automaticallyImplyLeading: false,
      ),
      body: SingleChildScrollView(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                key: _button1Key,
                onPressed: () {
                  debugPrint('Button 1 pressed');
                  if (_scaffoldKey.currentContext != null) {
                    _showTutorial();
                  } else {
                    debugPrint('Error: Scaffold context is null');
                  }
                },
                child: const Text('Button 1'),
              ),
              const SizedBox(height: 20),
              ElevatedButton(
                key: _button2Key,
                onPressed: () {
                  debugPrint('Button 2 pressed');
                  if (_scaffoldKey.currentContext != null) {
                    debugPrint('Showing Snackbar for Button 2');
                    ScaffoldMessenger.of(_scaffoldKey.currentContext!).showSnackBar(
                      const SnackBar(
                        content: Text('Button 2 pressed'),
                        duration: Duration(seconds: 2),
                      ),
                    );
                  } else {
                    debugPrint('Error: Scaffold context is null');
                  }
                },
                child: const Text('Button 2'),
              ),
              ListView.builder(
                key: _listKey,
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: 3,
                itemBuilder: (context, index) {
                  return ListTile(
                    key: index == 0 ? _firstItemKey : null,
                    title: Text('Item $index'),
                    onTap: () {
                      debugPrint('List item $index tapped');
                      if (_scaffoldKey.currentContext != null) {
                        debugPrint('Showing Snackbar for item $index');
                        ScaffoldMessenger.of(_scaffoldKey.currentContext!).showSnackBar(
                          SnackBar(
                            content: Text('Item $index tapped'),
                            duration: const Duration(seconds: 2),
                          ),
                        );
                      }
                    },
                  );
                },
              ),

              Row(
                mainAxisAlignment: MainAxisAlignment.end,
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  ElevatedButton(
                    key: _button3Key,
                    onPressed: () {
                      debugPrint('Button 3 pressed');
                      if (_scaffoldKey.currentContext != null) {
                        _showTutorial();
                      } else {
                        debugPrint('Error: Scaffold context is null');
                      }
                    },
                    child: const Text('Button 3'),
                  ),
                ],
              ),

              ListView.builder(
                itemCount: 4,
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemBuilder: (context, index) {
                  const lastItemIndex = 3;
                  return ListTile(
                    key: index == lastItemIndex ? _lastItemKey : null,
                    title: Text('Item $index'),
                  );
                },
              ),

              Row(
                mainAxisAlignment: MainAxisAlignment.start,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  ElevatedButton(
                    key: _button4Key,
                    onPressed: () {
                      debugPrint('Button 4 pressed');
                      _showTutorial();
                    },
                    child: const Text('Button 4'),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _setupMethodChannel() {
    _methodChannel.setMethodCallHandler((call) async {
      if (call.method == 'showMessage') {
        final message = call.arguments as String;
        debugPrint('Received message from Android: $message');
        if (_scaffoldKey.currentContext != null) {
          ScaffoldMessenger.of(_scaffoldKey.currentContext!).showSnackBar(
            SnackBar(content: Text(message)),
          );
        }
      }
    });
  }
  
  Future<void> _setupTutorial() async {
    _tutorialManager ??= await TutorialManager.getInstance(context);
    
    // Wait for the next frame to ensure all widgets are built
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      if (!_tutorialManager!.hasShownTutorial('scenario_tutorial')) {
        debugPrint('Setting up tutorial...');
        
        // Verify keys are attached to widgets
        debugPrint('Button1 key attached: ${_button1Key.currentContext != null}');
        debugPrint('Button2 key attached: ${_button2Key.currentContext != null}');
        debugPrint('List key attached: ${_listKey.currentContext != null}');
        
        final tutorial = Tutorial(
          'scenario_tutorial',
          [
            TutorialStep(
              key: _button1Key,
              title: 'Button 1',
              message: 'This is the first button. Tap it to show a message.',
            ),
            TutorialStep(
              key: _button2Key,
              title: 'Button 2',
              message: 'This is the second button. It also shows a message when tapped.',
            ),
            TutorialStep(
              key: _firstItemKey,
              title: 'Item đầu tiên',
              message: 'Đây là phần tử đầu tiên của danh sách. Bấm vào để xem chi tiết.',
            ),
            TutorialStep(
              key: _button3Key,
              title: 'Button 3',
              message: 'This is the third button. It also shows a message when tapped.',
            ),
            TutorialStep(
              key: _lastItemKey,
              title: 'Item cuối cùng',
              message: 'Đây là phần tử cuối cùng của danh sách. Bấm vào để xem chi tiết.',
            ),
            TutorialStep(
              key: _button4Key,
              title: 'Button 4',
              message: 'This is the fourth button. It also shows a message when tapped.',
            ),
          ],
        );
        
        debugPrint('Showing tutorial...');
        _tutorialManager!.showTutorial(tutorial);
      } else {
        debugPrint('Tutorial already shown');
      }
    });
  }

  Future<void> _showTutorial() async {
    _tutorialManager ??= await TutorialManager.getInstance(context);
    
    // Wait for the next frame to ensure all widgets are built
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      debugPrint('Setting up tutorial...');
      
      // Verify keys are attached to widgets
      debugPrint('Button1 key attached: ${_button1Key.currentContext != null}');
      debugPrint('Button2 key attached: ${_button2Key.currentContext != null}');
      debugPrint('List key attached: ${_listKey.currentContext != null}');
      
      final tutorial = Tutorial(
        'scenario_tutorial',
        [
          TutorialStep(
            key: _button1Key,
            title: 'Button 1',
            message: 'Đây là nút đầu tiên. Bấm vào đây để hiển thị hướng dẫn.',
          ),
          TutorialStep(
            key: _button2Key,
            title: 'Button 2',
            message: 'Đây là nút thứ hai. Bấm vào đây để hiển thị thông báo.',
          ),
          TutorialStep(
            key: _firstItemKey,
            title: 'Item đầu tiên',
            message: 'Đây là phần tử đầu tiên của danh sách. Bấm vào để xem chi tiết.',
          ),
          TutorialStep(
            key: _button3Key,
            title: 'Button 3',
            message: 'Đây là nút thứ ba. Bấm vào đây để hiển thị thông báo.',
          ),
          TutorialStep(
            key: _lastItemKey,
            title: 'Item cuối cùng',
            message: 'Đây là phần tử cuối cùng của danh sách. Bấm vào để xem chi tiết.',
          ),
          TutorialStep(
            key: _button4Key,
            title: 'Button 4',
            message: 'Đây là nút thứ tư. Bấm vào đây để hiển thị thông báo.',
          ),
        ],
      );
      
      debugPrint('Showing tutorial...');
      _tutorialManager!.showTutorial(tutorial);
    });
  }
}
