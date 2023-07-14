package cn.albresky.splayer.Utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

public class AnimationUtils {

    public static ObjectAnimator getRotateAnimation(Object obj) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(obj, "rotation", 0f, 360f);//添加旋转动画，旋转中心默认为控件中点
        rotateAnimator.setDuration(12000);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.setRepeatMode(ValueAnimator.RESTART);
        return rotateAnimator;
    }
}
