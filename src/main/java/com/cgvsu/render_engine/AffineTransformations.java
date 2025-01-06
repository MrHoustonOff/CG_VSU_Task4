package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
//import com.cgvsu.math.Quaternion;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;

import static java.lang.Math.cos;
import static java.lang.Math.sin;


/**
 * @author <a href="https://vk.com/v_zubkin">Мельник Василий</a>, ФКН 2 группа 2 курс<br>
 * <mark><b><i><u>"Каждый раз, когда вы пишете комментаний, поморщитесь и ощутите свою неудачу"</u></i></b> <br></mark>
 * <b><i><u>*никчемность</u></i></b> <br>
 *  при исрпользовании данной библиотеки рекомендую юзать мою библиотеку матеши:
 *  <a href="https://github.com/lagrodev/mathematics-Library.git">Математика</a>
 */

public class AffineTransformations {
    private static boolean quaternionRotate = false;

    public static void setMatrixRotate(boolean matrixRota) {
        quaternionRotate = matrixRota;
    }

    public static Matrix4f rotate(float alpha, float beta, float gamma) {
//        if (quaternionRotate) {
//            //todo понять, что с кватернионами не так - где-то не так умножение кватернионов... фак
//            //return rotateIntoQuaternion(alpha, beta, gamma);
//        }
        return rotateIntoMatrix(alpha, beta, gamma);
    }


    /**
     * <p>Аффинные преобразования - поворот с помощью Кватернионов <br>
     * Поехали с базы. Кватернион - шо це таки? Кватернион - грубо говоря, комплексное число в 3d мире, т.е. q=a+bi+cj+dk
     * тут a,b,c,d - вещественные числа, i,j,k - мнимые.
     * </p>
     * <p>Свойства этой хрени:
     * </p>
     * <p>1) i<sup>2</sup>=j<sup>2</sup>=k<sup>2</sup>=ijk=-1</p>
     * <p>2) вращение по 2м осям должны давать 3ю ось (причем вращение в линейном порядке - подобно операциям с матрицами)
     * другими словами, ij=k, jk=i, ki=j
     * </p>
     * Тут до обновления была туева куча инфы :) Но я все удалил, поэтому не расстраиваемся, берем все в кулак и погнали
     * <p>
     * Пойдем с базовой хрени работы с кватернионами. Предположим, что нам надо умножить 2 кватерниона без действительных частей, т.е.
     * p и q, тогда их перемножение:
     * <p>pq = [a*b]-'a;b' <br>
     * тут 'a;b' - скалярное произведение кватернионов
     * </p>
     * </p>
     *
     * <p>Отлично, вроде все базовое вспомнил. теперь посмотрим как это говно использовать в повороте.<br>
     * Поворот вокруг оси (A) на угол alpha описывается в виде:
     * q = cos(alpha/2) + asin(alpha/2), причем |a| = 1;
     * q * (q<sup>*</sup>) = cos(alpha/2) + asin(alpha/2)(cos(alpha/2) - asin(alpha/2)) = 1
     * </p>
     * <p>
     * (q<sup>-1</sup>) = cos(alpha/2) - asin(alpha/2) = (q<sup>*</sup>)
     * </p>
     *
     * <p>
     * Предположим, нам надо повернуть вокруг оси X. тогда, нам нгадо произвести след. умножение: <br>
     * x' =    q x (q<sup>*</sup>) = (xcos(alpha/2) + axsin(alpha/2))(cos(alpha/2) - asin(alpha/2))<br>
     * причем (xcos(alpha/2) + axsin(alpha/2)) - это кватернионное произведение <br>
     * <b>xcos^2(alpha/2) + sin(alpha/2)cos(alpha/2) (ax-xa) - axasin^2(alpha/2)<br></b>
     * ax = - 'a;x' + [a*x];<br>
     * xa = -'a;x' - [a*x];<br> =>
     * <b> (ax-xa) = 2[a*x]<br></b>
     * </p>
     * <p>
     * x' = xcos^2(alpha/2) + sin(alpha/2)cos(alpha/2)*2[a*x] - axasin^2(alpha/2)</b>
     * a(xa) = a ([x*a] - 'x;a') = -'a;[x*a]' - a'x*a' + [a*x*a] = 0 - a'a;x' + x'a;a' - a'a;x' = x -2a'a;x'
     * </p>
     * <p>
     * Тогда x' = xcos^2(alpha/2) + sin(alpha/2)cos(alpha/2)*2[a*x] - sin^2(alpha/2)(2a'x;a' - x) =
     * x (cos^2(alpha/2)) + [a*x]2sin(alpha/2)cos(alpha/2) +a'x;a'2sin^2(alpha/2) =
     * xcos(alpha) + [a*x]sin(alpha) + a'x;a' - a'x;a'cos(alpha) = (x - a'x;a')cos(alpha) + [a*x]sin(alpha) + a'x;a'
     * </p>
     * понял ли я? ну.. нет
     * <p>
     * Обобщим это говно: мы имеем, что для поворота кватернион задается как q =  cos(alpha/2) + v*sin(alpha/2),
     * v — единичный вектор, направленный вдоль оси вращения, при этом реальная часть кватерниона = cos(alpha/2)
     * </p>
     *
     * @return {@code Matrix4f} - матрица поворота
     */

//    private static Matrix4f rotateIntoQuaternion(float alpha, float beta, float gamma) {
//        // вокруг x
//
//        alpha = (float) Math.toRadians(alpha);
//        beta = (float) Math.toRadians(beta);
//        gamma = (float) Math.toRadians(gamma);
//
//        Quaternion x = new Quaternion((float) cos(alpha / 2), (float) sin(alpha / 2), 0, 0);
//        Quaternion y = new Quaternion((float) cos(beta / 2), 0, (float) sin(beta / 2), 0);
//        Quaternion z = new Quaternion((float) cos(gamma / 2), 0, 0, (float) sin(gamma / 2));
//
//        // сначала крутим вокруг x, потом y, потом z
//
//        z.multiply(x);
//        z.multiply(y);
//        z.normalize();
//
//        return quaternionToMatrix(z);
//    }
//
//    /**
//     * <a href = "https://gamedev.ru/code/articles/?id=4215&page=2">я это прочитаю</a>
//     *
//     * @param quaternion
//     * @return {@code Matrix4f} - матрица поворота
//     */
//    // не важно, откуда я это спер...
//    public static Matrix4f quaternionToMatrix(Quaternion quaternion) {
//
//        float w = quaternion.getW();
//        float x = -quaternion.getX();
//        float y = -quaternion.getY();
//        float z = -quaternion.getZ();
//
//        float[] matrix = new float[]{
//                2 * (w * w + x * x) - 1, 2 * (x * y - w * z), (x * z + w * y), 0,
//                2 * (x * y + w * z), 2 * (w * w + y * y) - 1, 2 * (y * z - w * x), 0,
//                2 * (x * z - w * y), 2 * (y * z + w * x), 2 * (w * w + z * z) - 1, 0,
//                0, 0, 0, 1
//        };
//
//
//        return new Matrix4f(matrix);
//    }

    /**
     * Поворот, за основу берем матрицы
     *
     * @param alpha угол вокруг x
     * @param beta  угол вокруг y
     * @param gamma угол вокруг z
     * @return {@code Matrix4f} матрица поворота
     */
    private static Matrix4f rotateIntoMatrix(float alpha, float beta, float gamma) {
        // вокруг x
        float[] rotateX = new float[]{
                1, 0, 0, 0,
                0, (float) cos(Math.toRadians(alpha)), (float) sin(Math.toRadians(alpha)), 0,
                0, (float) -sin(Math.toRadians(alpha)), (float) cos(Math.toRadians(alpha)), 0,
                0, 0, 0, 1
        };
        // вокруг y
        float[] rotateY = new float[]{
                (float) cos(Math.toRadians(beta)), 0, (float) sin(Math.toRadians(beta)), 0,
                0, 1, 0, 0,
                (float) -sin(Math.toRadians(beta)), 0, (float) cos(Math.toRadians(beta)), 0,
                0, 0, 0, 1
        };
        // вокруг z
        float[] rotateZ = new float[]{
                (float) cos(Math.toRadians(gamma)), (float) sin(Math.toRadians(gamma)), 0, 0,
                (float) -sin(Math.toRadians(gamma)), (float) cos(Math.toRadians(gamma)), 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        };

        Matrix4f rotateAboutX = new Matrix4f(rotateX);
        Matrix4f rotateAboutY = new Matrix4f(rotateY);
        Matrix4f rotateAboutZ = new Matrix4f(rotateZ);

        // сначала крутим вокруг x, потом y, потом z (да, в реале умножаем сначала Z, потом ыгрик, потом х (а) - плохо)
        // вот нихрена не фАКТ
        rotateAboutZ.multiply(rotateAboutY);
        rotateAboutZ.multiply(rotateAboutX);

        return rotateAboutZ;
    }


    /**
     * Аффинные преобразования - увеличение/уменьшение объекта
     *
     * @param vector3f вектор переноса
     * @return {@code Matrix4f} - матрица переноса
     */


    public static Matrix4f translate(Vector3f vector3f) {
        return calculateTranslate(new Vector4f(vector3f.getX(), vector3f.getY(), vector3f.getZ(), 1));
    }

    private static Matrix4f calculateTranslate(Vector4f vector) {
        Matrix4f res = new Matrix4f(1);
        for (int i = 0; i < res.getElements().length - 1; i++) {
            res.setElement(i, 3, vector.getNum(i));
        }
        return res;
    }


    /**
     * Аффинные преобразования - увеличение/уменьшение объекта
     *
     * @param scaleX растяжение по x
     * @param scaleY растяжение по y
     * @param scaleZ растяжение по z
     * @return {@code Matrix4f} - матрица растяжения (по факту просто по главной диагонали будут стоять не 1, а растяжение наше)
     */

    public static Matrix4f scale(float scaleX, float scaleY, float scaleZ) {
        float[] scale = new float[]{scaleX, scaleY, scaleZ};
        return calculateScale(scale);
    }


    private static Matrix4f calculateScale(float[] scale) {
        Matrix4f result = new Matrix4f(1);
        for (int i = 0; i < scale.length; i++) {
            result.setElement(i, i, scale[i]);
        }
        return result;
    }

    public Object getModelName() {
        return null;
    }
}