import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:async';

class TutorialStep {
  final GlobalKey key;
  final String title;
  final String message;

  TutorialStep({
    required this.key,
    required this.title,
    required this.message,
  });
}

class Tutorial {
  final String id;
  final List<TutorialStep> steps;
  OverlayEntry? overlayEntry;

  Tutorial(this.id, this.steps);
}

class TutorialManager {
  static TutorialManager? _instance;
  final BuildContext _context;
  final SharedPreferences _prefs;
  Tutorial? _currentTutorial;
  int _currentStepIndex = 0;
  Timer? _delayTimer;
  static const Duration _tutorialDelay = Duration(milliseconds: 500);

  TutorialManager._(this._context, this._prefs);

  static Future<TutorialManager> getInstance(BuildContext context) async {
    if (_instance == null) {
      final prefs = await SharedPreferences.getInstance();
      _instance = TutorialManager._(context, prefs);
    }
    return _instance!;
  }

  bool hasShownTutorial(String tutorialId) {
    return _prefs.getBool('tutorial_$tutorialId') ?? false;
  }

  void markTutorialAsShown(String tutorialId) {
    _prefs.setBool('tutorial_$tutorialId', true);
  }

  void resetTutorialStatus(String tutorialId) {
    _prefs.remove('tutorial_$tutorialId');
  }

  void showTutorial(Tutorial tutorial) {
    // Cancel any existing tutorial
    if (_currentTutorial != null) {
      _currentTutorial!.overlayEntry?.remove();
      _currentTutorial = null;
    }

    _currentTutorial = tutorial;
    _currentStepIndex = 0;
    
    // Show a snackbar to inform user about tutorial activation
    ScaffoldMessenger.of(_context).showSnackBar(
      const SnackBar(content: Text('Tutorial activated')),
    );

    // Add a delay to ensure UI is properly laid out
    _delayTimer?.cancel();
    _delayTimer = Timer(_tutorialDelay, () {
      _showCurrentStep();
    });
  }

  void _showCurrentStep() {
    if (_currentTutorial == null || _currentStepIndex >= _currentTutorial!.steps.length) {
      return;
    }

    final step = _currentTutorial!.steps[_currentStepIndex];
    final renderBox = step.key.currentContext?.findRenderObject() as RenderBox?;
    
    if (renderBox == null) {
      debugPrint('Could not find render box for step ${_currentStepIndex}');
      return;
    }

    final position = renderBox.localToGlobal(Offset.zero);
    final size = renderBox.size;

    // Remove any existing overlay
    _currentTutorial!.overlayEntry?.remove();

    // Create new overlay
    _currentTutorial!.overlayEntry = OverlayEntry(
      builder: (context) => _TutorialOverlay(
        position: position,
        size: size,
        step: step,
        onNext: _nextStep,
        onSkip: cancelTutorial,
      ),
    );

    // Insert overlay
    Overlay.of(_context).insert(_currentTutorial!.overlayEntry!);
  }

  void _nextStep() {
    _currentStepIndex++;
    if (_currentStepIndex >= _currentTutorial!.steps.length) {
      _finishTutorial();
    } else {
      _showCurrentStep();
    }
  }

  void _finishTutorial() {
    if (_currentTutorial != null) {
      markTutorialAsShown(_currentTutorial!.id);
      _currentTutorial!.overlayEntry?.remove();
      _currentTutorial = null;
    }
  }

  void cancelTutorial() {
    _delayTimer?.cancel();
    if (_currentTutorial != null) {
      _currentTutorial!.overlayEntry?.remove();
      _currentTutorial = null;
    }
  }
}

class _TutorialOverlay extends StatelessWidget {
  final Offset position;
  final Size size;
  final TutorialStep step;
  final VoidCallback onNext;
  final VoidCallback onSkip;
  final double dashPadding;
  final double tooltipPadding;

  const _TutorialOverlay({
    required this.position,
    required this.size,
    required this.step,
    required this.onNext,
    required this.onSkip,
    this.dashPadding = 10,
    this.tooltipPadding = 16,
  });

  @override
  Widget build(BuildContext context) {
    final screenSize = MediaQuery.of(context).size;
    // Tooltip width
    const double tooltipWidth = 280;
    const double tooltipInnerPadding = 12;
    const double arrowHeight = 10;
    const double arrowWidth = 20;
    const double borderRadius = 16;
    const double highlightRadius = 5;
    const double dashLength = 7;
    const double dashGap = 4;

    // Calculate dash rect
    final dashRect = Rect.fromLTWH(
      position.dx - dashPadding,
      position.dy - dashPadding,
      size.width + 2 * dashPadding,
      size.height + 2 * dashPadding,
    );
    final dashRadius = highlightRadius + dashPadding;

    // Tooltip position (always outside dashline)
    final isTopHalf = position.dy < screenSize.height / 2;
    double tooltipLeft = position.dx + size.width / 2 - tooltipWidth / 2;
    if (tooltipLeft < 8) tooltipLeft = 8;
    if (tooltipLeft + tooltipWidth > screenSize.width - 8) {
      tooltipLeft = screenSize.width - tooltipWidth - 8;
    }
    double tooltipTop = isTopHalf
        ? dashRect.bottom + tooltipPadding
        : dashRect.top - tooltipPadding - arrowHeight - 48; // 48: min tooltip height
    if (!isTopHalf && tooltipTop < 8) tooltipTop = 8;

    debugPrint('[TUTORIAL] Target position: $position, size: $size');
    debugPrint('[TUTORIAL] DashRect: $dashRect, dashRadius: $dashRadius');
    debugPrint('[TUTORIAL] TooltipLeft: $tooltipLeft, TooltipTop: $tooltipTop');
    debugPrint('[TUTORIAL] isTopHalf: $isTopHalf');

    return Stack(
      children: [
        // Custom overlay with transparent hole and dashed border
        Positioned.fill(
          child: GestureDetector(
            onTap: onNext,
            child: CustomPaint(
              painter: _OverlayWithDashedBorderPainter(
                highlightRect: Rect.fromLTWH(
                  position.dx,
                  position.dy,
                  size.width,
                  size.height,
                ),
                borderRadius: highlightRadius,
                dashLength: dashLength,
                dashGap: dashGap,
                dashDistance: dashPadding,
              ),
              child: Container(),
            ),
          ),
        ),
        // Tooltip bubble (with arrow)
        Positioned(
          left: tooltipLeft,
          top: isTopHalf ? dashRect.bottom + (tooltipPadding - dashPadding) : null,
          bottom: !isTopHalf ? (screenSize.height - dashRect.top + (tooltipPadding - dashPadding)) : null,
          child: Material(
            color: Colors.transparent,
            child: _TooltipBubble(
              width: tooltipWidth,
              borderRadius: borderRadius,
              arrowHeight: arrowHeight,
              arrowWidth: arrowWidth,
              isArrowDown: !isTopHalf,
              arrowCenter: (position.dx + size.width / 2) - tooltipLeft,
              child: Builder(
                builder: (context) {
                  final box = context.findRenderObject() as RenderBox?;
                  debugPrint('[TUTORIAL] Tooltip bubble size: \\${box?.size}');
                  return Padding(
                    padding: const EdgeInsets.symmetric(
                      horizontal: tooltipInnerPadding,
                      vertical: 16,
                    ),
                    child: Center(
                      child: Text(
                        step.message,
                        style: const TextStyle(
                          color: Color(0xFF1976D2), // blue
                          fontSize: 16,
                          fontWeight: FontWeight.w500,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  );
                },
              ),
            ),
          ),
        ),
      ],
    );
  }
}

class _OverlayWithDashedBorderPainter extends CustomPainter {
  final Rect highlightRect;
  final double borderRadius;
  final double dashLength;
  final double dashGap;
  final double dashDistance;

  _OverlayWithDashedBorderPainter({
    required this.highlightRect,
    required this.borderRadius,
    required this.dashLength,
    required this.dashGap,
    this.dashDistance = 10,
  });

  @override
  void paint(Canvas canvas, Size size) {
    // Draw dark overlay with a transparent hole exactly at the target
    final overlayPaint = Paint()
      ..color = Colors.black.withOpacity(0.6)
      ..style = PaintingStyle.fill;
    final overlayPath = Path()..addRect(Rect.fromLTWH(0, 0, size.width, size.height));
    final highlightPath = Path()
      ..addRRect(RRect.fromRectAndRadius(highlightRect, Radius.circular(borderRadius)));
    final combined = Path.combine(PathOperation.difference, overlayPath, highlightPath);
    canvas.drawPath(combined, overlayPaint);

    // Draw dashed border 10px outside the target
    final borderPaint = Paint()
      ..color = const Color(0xFF1976D2)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2;
    final dashRect = Rect.fromLTWH(
      highlightRect.left - dashDistance,
      highlightRect.top - dashDistance,
      highlightRect.width + 2 * dashDistance,
      highlightRect.height + 2 * dashDistance,
    );
    final dashRadius = borderRadius + dashDistance;
    _drawDashedRRect(
      canvas,
      RRect.fromRectAndRadius(dashRect, Radius.circular(dashRadius)),
      borderPaint,
      dashLength,
      dashGap,
    );
  }

  void _drawDashedRRect(Canvas canvas, RRect rrect, Paint paint, double dashLength, double dashGap) {
    final path = Path()..addRRect(rrect);
    final metrics = path.computeMetrics().toList();
    for (final metric in metrics) {
      double distance = 0.0;
      while (distance < metric.length) {
        final next = distance + dashLength;
        final extractPath = metric.extractPath(distance, next);
        canvas.drawPath(extractPath, paint);
        distance = next + dashGap;
      }
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}

class _TooltipBubble extends StatelessWidget {
  final double width;
  final double borderRadius;
  final double arrowHeight;
  final double arrowWidth;
  final bool isArrowDown;
  final double arrowCenter;
  final Widget child;

  const _TooltipBubble({
    required this.width,
    required this.borderRadius,
    required this.arrowHeight,
    required this.arrowWidth,
    required this.isArrowDown,
    required this.arrowCenter,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      painter: _TooltipBubblePainter(
        borderRadius: borderRadius,
        arrowHeight: arrowHeight,
        arrowWidth: arrowWidth,
        isArrowDown: isArrowDown,
        arrowCenter: arrowCenter,
        width: width,
      ),
      child: Container(
        width: width,
        margin: EdgeInsets.only(
          top: isArrowDown ? 0 : arrowHeight,
          bottom: isArrowDown ? arrowHeight : 0,
        ),
        alignment: Alignment.center,
        child: child,
      ),
    );
  }
}

class _TooltipBubblePainter extends CustomPainter {
  final double borderRadius;
  final double arrowHeight;
  final double arrowWidth;
  final bool isArrowDown;
  final double arrowCenter;
  final double width;

  _TooltipBubblePainter({
    required this.borderRadius,
    required this.arrowHeight,
    required this.arrowWidth,
    required this.isArrowDown,
    required this.arrowCenter,
    required this.width,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = Colors.white
      ..style = PaintingStyle.fill
      ..isAntiAlias = true;
    final rrect = RRect.fromLTRBR(
      0,
      isArrowDown ? 0 : arrowHeight,
      width,
      size.height - (isArrowDown ? arrowHeight : 0),
      Radius.circular(borderRadius),
    );
    final path = Path()..addRRect(rrect);
    // Arrow
    final arrowX = arrowCenter.clamp(borderRadius + arrowWidth / 2, width - borderRadius - arrowWidth / 2);
    if (isArrowDown) {
      path.moveTo(arrowX - arrowWidth / 2, size.height - arrowHeight);
      path.lineTo(arrowX, size.height);
      path.lineTo(arrowX + arrowWidth / 2, size.height - arrowHeight);
    } else {
      path.moveTo(arrowX - arrowWidth / 2, arrowHeight);
      path.lineTo(arrowX, 0);
      path.lineTo(arrowX + arrowWidth / 2, arrowHeight);
    }
    path.close();
    canvas.drawShadow(path, Colors.black.withOpacity(0.12), 8, false);
    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
} 