import matplotlib.pyplot as plt
import numpy as np

'''
如果在plot函数中，只有一个单独的list，那么这个list中的值将会默认被作为y轴的取值点
'''
plt.plot([1, 2, 3, 4])
plt.ylabel('Some Numbers')
plt.show()

'''
第一个数组作为x轴的取值点，第二个数组作为y轴的取值点
'''
plt.plot([1, 2, 3, 4], [5, 6, 7, 10])
plt.ylabel('Some More Numbers')
plt.show()

'''
格式化输出风格
对于x,y这一对参数，有一个可选的第三个参数，是一个格式化的字符串可以指定线条的颜色以及类型，
该字符串中的这些字母和符号来源于MATLAB，并且你可以将颜色字符串和线条类型字符串进行拼接。默认的格式化字符串
是'b-' 也就是蓝色的、实线，，我们可以设置为红色的圆圈。
plot支持的颜色属性表如下所示：
character	    color
====================================
    'b'    |    blue
    'g'    |    green
    'r'    |    red
    'c'    |    cyan
    'm'    |    magenta
    'y'    |    yellow
    'k'    |    black
    'w'    |    white
    
marker:
character	    color
=====================================
'.'	        |       point marker
','	        |       pixel marker
'o'	        |       circle marker
'v'	        |       triangle_down marker
'^'	        |       triangle_up marker
'<'	        |       triangle_left marker
'>'	        |       triangle_right marker
'1'	        |       tri_down marker
'2'	        |       tri_up marker
'3'	        |       tri_left marker
'4'	        |       tri_right marker
's'	        |       square marker
'p'	        |       pentagon marker
'*'	        |       star marker
'h'	        |       hexagon1 marker
'H'	        |       hexagon2 marker
'+'	        |       plus marker
'x'	        |       x marker
'D'	        |       diamond marker
'd'	        |       thin_diamond marker
'|'	        |       vline marker
'_'	        |       hline marker
    
线的类型表：

character	description
==========================
'-'	    |   solid line style
'--'	|   dashed line style
'-.'	|   dash-dot line style
':'	    |   dotted line style

'''
plt.plot([1, 2, 3, 4], [1, 4, 9, 16], 'ro')
plt.axis([0, 6, 0, 20])
plt.show()

'''
如果matplotlib仅限于处理列表，那么它对数字处理就没什么用处了。通常，可以使用numpy数组。实际上，所有序列都在内部转换成numpy数组
'''

arr = np.arange(0., 5, 0.2)
# 可以直接画出三条线，每条线的参数格式就是：x,y,linetype
plt.plot(arr, arr, 'r--', arr, arr ** 2, 'bs', arr, arr ** 3, 'g^')
plt.show()
