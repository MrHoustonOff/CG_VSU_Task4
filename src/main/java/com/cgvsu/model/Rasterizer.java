package com.cgvsu.model;



import com.cgvsu.math.AbstractMatrix;
import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.GraphicConveyor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

//Вершина
class Vertex {
    float x;
    float y;
    float z;
    public Vertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
//Полигон
class Polygonz {
    List<Vertex> vertices;
    Color color;
    public Polygonz(List<Vertex> vertices, Color color) {
        this.vertices = vertices;
        this.color = color;
    }
    public List<Vertex> getVertices() {
        return vertices;
    }
    public Color getColor() {
        return color;
    }
}

//Класс для растеризации
public class Rasterizer {
    private final int width;
    private final int height;
    private final BufferedImage image;
    private float[][] zBuffer;
   // private Camera camera;
    private ArrayList<Vector3f> vertices;
    private final Camera camera;


    public Rasterizer(int width, int height, Camera camera, ArrayList<Vector3f> modelVertices) {
        this.width = width;
        this.height = height;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.zBuffer = new float[width][height];
       // System.out.println("        65"  + Arrays.deepToString(zBuffer));

        this.camera = camera;
        this.vertices = modelVertices;

    }

    public static float[] getCentroid(Polygon polygon, ArrayList<Vector3f> vertices) {
        float centroidX = 0;
        float centroidY = 0;
        if (polygon.getVertexIndices().isEmpty()){
            return new float[]{0, 0};
        }
        System.out.println("Vertices of polygon");
        for (int index : polygon.getVertexIndices()) {
            System.out.println("  vertex x=" + vertices.get(index).getX() + " y=" + vertices.get(index).getY());
            centroidX += vertices.get(index).getX();
            centroidY += vertices.get(index).getY();
        }
        centroidX /= polygon.getVertexIndices().size();
        centroidY /= polygon.getVertexIndices().size();
        System.out.println("Centroid: x = " + centroidX + ", y = " + centroidY);
        return new float[]{centroidX, centroidY};
    }

    private void clearZBuffer() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                zBuffer[i][j] = Float.NEGATIVE_INFINITY;
            }
        }
    }
    public BufferedImage rasterizePolygon(GraphicsContext graphicsContext, Polygon pol) {
        clearZBuffer();
        BufferedImage image1 = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);

        List<Integer> indices = pol.getVertexIndices();
  //      System.out.println("indices = : " + indices);

        if(indices.size() != 3) {
            System.err.println("This rasterizer needs triangle as a polygon");
            return image;
        }
//        for (int i = 0; i < 10; i++) {
//            System.out.println("modelVertices = : " + this.vertices.get(i));
//
//        }
            float[] polygon = new float[]{
                    this.vertices.get(indices.get(0)).getX(), this.vertices.get(indices.get(0)).getY(), this.vertices.get(indices.get(0)).getZ(),
                    this.vertices.get(indices.get(1)).getX(), this.vertices.get(indices.get(1)).getY(), this.vertices.get(indices.get(1)).getZ(),
                    this.vertices.get(indices.get(2)).getX(), this.vertices.get(indices.get(2)).getY(), this.vertices.get(indices.get(2)).getZ()

            };

        //for (int i = 0; i < polygon.length; i++) {
   //         System.out.println("polygon = : " + Arrays.toString(polygon));

     //   }


        float[] projected = projectTriangle(polygon);

       // for (int i = 0; i < Objects.requireNonNull(projected).length; i++) {
   //         System.out.println("projected = : " + Arrays.toString(projected));

      //  }


        if(projected == null)
            return image;

        float minX = Math.min(projected[0], Math.min(projected[3], projected[6]));
        float maxX = Math.max(projected[0], Math.max(projected[3], projected[6]));
        float minY = Math.min(projected[1], Math.min(projected[4], projected[7]));
        float maxY = Math.max(projected[1], Math.max(projected[4], projected[7]));

        System.out.println("minX = : " + minX);
        System.out.println("maxX = : " + maxX);
        System.out.println("minY = : " + minY);
        System.out.println("maxY = : " + maxY);

        float[] centroid = getCentroid(pol, this.vertices);
        pol.setPosition(centroid[0] + (float) this.width /2, centroid[1] + (float) this.height/2);
        //  pol.setPosition(minX, minY);

//        for (int i = 0; i < projected.length; i += 3) {
//            float x = projected[i];
//            float y = projected[i + 1];
//
//            minX = Math.min(minX, x);
//            maxX = Math.max(maxX, x);
//            minY = Math.min(minY, y);
//            maxY = Math.max(maxY, y);
//        }

     //   System.out.println("До цикла");


        for (int y = (int) Math.floor(minY); y <= (int) Math.floor(maxY); y++) {
      //      System.out.println("В первом цикле");
            for (int x = (int) Math.floor(minX); x <= (int) Math.floor(maxX); x++) {
       //         System.out.println("Во втором цикле");

                if (x < 0 || x >= width || y < 0 || y >= height) {
                    continue;
                }
              //  System.out.println("      170");

                float[] barycentric = calculateBarycentricCoordinates(x, y, projected);
           //     System.out.println("      173 = " + Arrays.toString(barycentric));
//
//                if (barycentric[0] < 0 || barycentric[1] < 0 || barycentric[2] < 0) {
//                    continue;
//                }
              //  System.out.println("      178");

                float z = polygon[2] * barycentric[0] + polygon[5] * barycentric[1] + polygon[8] * barycentric[2];
           //     System.out.println("      181 = " + z);

                if (z > zBuffer[x][y]) {
                    ///System.out.println("      184");

                    zBuffer[x][y] = z;
                    Color col = new Color(1,0,0, 0.5);
                    // int colorValue = convertColorToInt(color);
                    //   image.setRGB(x, y, colorValue);

                    //    System.out.println("image.setRGB(x = : " + x);
                    //   System.out.println("image.setRGB(y = : " + y);

                    int colorValue = convertColorToInt(col);
                    //   System.out.println("colorValue = : " + colorValue);

                    image1.setRGB(x, y, colorValue);
                   // System.out.println("xxxx" + image1);

                }
            }
        }


//        for (int y = (int) Math.floor(minY); y <= (int) Math.floor(maxY); y++) {
//            for (int x = (int) Math.floor(minX); x <= (int) Math.floor(maxX); x++) {
//
//                if (x < 0 || x >= width || y < 0 || y >= height) {
//                    continue;
//                }
//                float[] barycentric = calculateBarycentricCoordinates(x, y, projected);
//                if (barycentric[0] < 0 || barycentric[1] < 0 || barycentric[2] < 0) {
//                    continue;
//                }
//                float z = polygon[2] * barycentric[0] + polygon[5] * barycentric[1] + polygon[8] * barycentric[2];
//                if (z > zBuffer[x][y]) {
//                    zBuffer[x][y] = z;
//                    Color col = new Color(1,0,0, 0.5);
//
//
//                    image.setRGB(x, y, col.hashCode());
//
//                }
//            }
//        }


        System.out.println("1x: " + pol.getPositionX());
        System.out.println("1y: " + pol.getPositionY());

        System.out.println("xxxx" + image1);

        return image1;
    }






    private int convertColorToInt(Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        return (red << 16) | (green << 8) | blue;
    }

    private float[] projectTriangle(float[] polygon) {
        if (polygon.length != 9) {
            return null; // Проверка на треугольник
        }

        float[] projected = new float[9];
        // 1. Получение матрицы проекции
        Matrix4f projectionMatrix = getProjectionMatrix();
        // 2. Проекция вершин
        for (int i = 0; i < 3; i++) {
            Vector3f vertex = new Vector3f(polygon[i * 3], polygon[i * 3 + 1], polygon[i * 3 + 2]);
            Vector3f projectedVertex = projectVertex(vertex, projectionMatrix);
            projected[i * 3] = projectedVertex.getX();
            projected[i * 3 + 1] = projectedVertex.getY();
            projected[i * 3 + 2] = projectedVertex.getZ();
        }
        return projected;
    }

    private Matrix4f getProjectionMatrix() {
        float fov = camera.getFov(); // Угол обзора камеры
        float aspectRatio = camera.getAspectRatio();
        float nearPlane = camera.getNearPlane();
        float farPlane = camera.getFarPlane();

        float f = 1.0f / (float) Math.tan(fov / 2.0);
        return new Matrix4f(
                f / aspectRatio, 0, 0, 0,
                0, f, 0, 0,
                0, 0, (farPlane + nearPlane) / (nearPlane - farPlane), -1,
                0, 0, (2 * farPlane * nearPlane) / (nearPlane - farPlane), 0
        );
    }
    private Vector3f projectVertex(Vector3f vertex, Matrix4f projectionMatrix) {

        // 1. Преобразуем Vector3f в Vector4f (добавляем w = 1.0)
        Vector4f vertex4f = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1.0f);
        Matrix4f am = new Matrix4f(projectionMatrix);

        // 2. Умножаем Vector4f на матрицу проекции
        Vector4f transformed4f = am.multiply(vertex4f);

        // 3. Перспективное деление
        float w = transformed4f.getW(); // Получаем w
        if (w != 0) {
            transformed4f.setX(transformed4f.getX() / w);
            transformed4f.setY(transformed4f.getY() / w);
            transformed4f.setZ(transformed4f.getZ() / w);
        }
        // 4. Возвращаем Vector3f после деления и отбрасывания компоненты w
        return new Vector3f(transformed4f.getX(), transformed4f.getY(), transformed4f.getZ());
    }

    private float[] calculateBarycentricCoordinates(float x, float y, float[] projected) {
        float x1 = projected[0];
        float y1 = projected[1];
        float x2 = projected[3];
        float y2 = projected[4];
        float x3 = projected[6];
        float y3 = projected[7];

        float area =  Math.abs( 0.5f * ((x2 - x3) * (y1 - y3) - (x1 - x3) * (y2 - y3)));

     //   System.out.println("area = 0.5 * |(x2 - x3)(y1 - y3) - (x1 - x3)(y2 - y3)|" + Arrays.toString(projected));

        // float area = 0.5f * ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));
     //   System.out.println("     area = " + area);
        if (area == 0) {
            System.err.println("Error: деление на ноль, площадь треугольника равна 0.");
            return new float[]{-1, -1, -1}; //Возвращаем некорректные значения
        }

        float w1 = 0.5f * ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / area;
        float w2 = 0.5f * ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / area;
        float w3 = 1 - w1 - w2;
     //   System.out.println("      w1 = " + w1);
      //  System.out.println("      w2 = " + w2);
      //  System.out.println("      w3 = " + w3);
        return new float[]{w1, w2, w3};
    }

   // private final TriangleRasterizer triangleRasterizer;

//    public Rasterizer(int width, int height) {
//        this.triangleRasterizer = new TriangleRasterizer(width, height);
//    }

//    public BufferedImage rasterize(GraphicsContext graphicsContext, Polygon polygon) {
//        return rasterizePolygon(graphicsContext, polygon);
//    }

    public List<BufferedImage> rasterize(GraphicsContext graphicsContext, List<Polygon> polygons) {
        List<BufferedImage> images = new ArrayList<>();
        //System.out.println("polygons.size()" +  polygons);
        if (polygons == null || polygons.isEmpty()) {
            return images;
        }
        //int n = 0;
        for (Polygon polygon : polygons) {

            System.out.println("rrrr" + polygon);
            //BufferedImage im = rasterizePolygon(graphicsContext, polygon);
//            int x = rasterizePolygon(graphicsContext, polygon).getMinX();
//            int y = rasterizePolygon(graphicsContext, polygon).getMinY();
//            System.out.println("   2x: " + x );
            //System.out.println("   2y: " + y );
          //  System.out.println("qqqq" + 344);
            images.add(rasterizePolygon(graphicsContext, polygon));
           // System.out.println("zzzz" + images);
         //   images.add(rasterizePolygon(graphicsContext, polygon));
          //  System.out.println("rasterize " + rasterizePolygon(graphicsContext, polygon));
        }

        return images;
    }
    static  public Image convertBufferedImageToImage(BufferedImage bufferedImage) {
        if (bufferedImage == null) return null;
        WritableImage wr = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        PixelWriter pw = wr.getPixelWriter();
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                pw.setColor(x, y, convertAwtColorToFxColor(new java.awt.Color(bufferedImage.getRGB(x, y))));
            }
        }
        return wr;
    }
    static  public javafx.scene.paint.Color convertAwtColorToFxColor(java.awt.Color color) {
        return javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 255.0);
    }
}
























//import com.cgvsu.math.Vector2f;
//import com.cgvsu.math.Vector3f;
//import com.cgvsu.render_engine.Camera;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.image.Image;
//import javafx.scene.image.PixelWriter;
//import javafx.scene.image.WritableImage;
//import javafx.scene.paint.Color;
//
//import java.awt.image.BufferedImage;
//import java.util.ArrayList;
//import java.util.List;
//
//
////todo: Неактивен надо вызвать где то или сделать галочку переключения в интерфейсе
//
//
//
//
//
//public class Rasterizer {
//
//
//    private final int width;  // Ширина изображения
//    private final int height; // Высота изображения
//    private final BufferedImage image; // Изображение, на котором рисуем
//    private final ModelTriangulator modelTriangulator; // Объект для триангуляции и вычисления нормалей
//    private final Camera camera; // Объект камеры
//    private float[][] zBuffer; // Z-буфер для отсечения невидимых пикселей
//    private int colorValue;
//
//
//
//    public static ArrayList<Polygon> setColor(ArrayList<Polygon> list_polygon, Color color) {
//        ArrayList<Polygon> polygon = new ArrayList<>();
//
//        System.err.println("setColor");
//        Polygon poly;
//        //  Здесь нужно закрасить list_polygon и скопировать его в polygon и вернуть polygon
//        if(list_polygon == null || list_polygon.isEmpty()) {
//            return polygon;
//        }
//        for (Polygon p : list_polygon) {
//            // Создаем новый полигон с тем же списком вершин и заданным цветом.
//            poly = new Polygon();
//            poly.setNormalIndices(p.getNormalIndices());
//            poly.setVertexIndices(p.getVertexIndices());
//            poly.setTextureVertexIndices(p.getTextureVertexIndices());
//            poly.setColor(color);
//
////            BufferedImage = rasterize();
////            List<Vector3f> vertices,
////            List<ModelTriangulator.Triangle> triangles,
////            Color color, Vector3f lightDirection,
////            BufferedImage texture,
////            List<Vector2f> textureVertices
//
//
//                polygon.add(poly);
//        }
//
//
//        return polygon;
//    }
//
//
//
//        /**
//         * Конструктор класса Rasterizer.
//         *
//         * @param width             Ширина изображения.
//         * @param height            Высота изображения.
//         * @param modelTriangulator Объект для триангуляции и вычисления нормалей.
//         * @param camera            Объект камеры.
//         */
//
//
//        public Rasterizer(int width, int height, ModelTriangulator modelTriangulator, Camera camera) {
//            this.width = width;
//            this.height = height;
//            this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//            this.modelTriangulator = modelTriangulator;
//            this.camera = camera;
//            this.zBuffer = new float[width][height]; // Инициализация Z-буфера
//        }
//
//        /**
//         * Метод для очистки Z-буфера.
//         * Заполняет Z-буфер отрицательной бесконечностью, чтобы новые пиксели гарантированно перекрывали старые.
//         */
//
//
//        public void clearZBuffer() {
//            for (int i = 0; i < width; i++) {
//                for (int j = 0; j < height; j++) {
//                    zBuffer[i][j] = Float.NEGATIVE_INFINITY;
//                }
//            }
//        }
//
//        /**
//         * Метод для растеризации модели.
//         *
//         * @param vertices       Список вершин модели.
//         * @param triangles      Список треугольников модели.
//         * @param color          Цвет заполнения полигонов.
//         * @param lightDirection Направление света для расчета освещения.
//         * @id865676420 (@ return) Изображение с отрисованной моделью.
//         */
//
//        public BufferedImage rasterize(List<Vector3f> vertices, List<ModelTriangulator.Triangle> triangles, Color color, Vector3f lightDirection, BufferedImage texture, List<Vector2f> textureVertices) {
//            System.out.println("Rasterizing started. Triangles count: " + triangles.size());
//
//            clearZBuffer(); // Очищаем Z-буфер перед началом рендеринга
//            int triangleCount = 0;
//            for (ModelTriangulator.Triangle triangle : triangles) {
//                if (triangleCount > 100) {
//                    System.out.println("Triangle count exceeded 100, stopping rasterization.");
//                    break;
//                }
//                rasterizeTriangle(triangle, vertices, color, lightDirection, texture, textureVertices); // Растеризуем каждый треугольник
//                triangleCount++;
//            }
//            System.out.println("Rasterizing finished.");
//            return image; // Возвращаем отрендеренное изображение
//        }
//
//        /**
//         * Метод для растеризации одного треугольника.
//         *
//         * @param triangle       Треугольник для растеризации.
//         * @param vertices       Список вершин модели.
//         * @param color          Цвет заполнения полигона.
//         * @param lightDirection Направление света для расчета освещения.
//         */
//
//        private void rasterizeTriangle(ModelTriangulator.Triangle triangle, List<Vector3f> vertices, Color color, Vector3f lightDirection, BufferedImage texture, List<Vector2f> textureVertices) {
//            int v1 = triangle.v1; // Индекс первой вершины
//            int v2 = triangle.v2; // Индекс второй вершины
//            int v3 = triangle.v3; // Индекс третьей вершины
//
//            // Проверка на корректность индексов вершин
//            if (v1 < 0 || v1 >= vertices.size() || v2 < 0 || v2 >= vertices.size() || v3 < 0 || v3 >= vertices.size()) {
//                System.err.println("Error: Invalid vertex index in triangle: v1=" + v1 + ", v2=" + v2 + ", v3=" + v3 + ". Vertices size: " + vertices.size());
//                return;
//            }
//
//            // Получаем вершины из списка
//            Vector3f vert1 = vertices.get(v1);
//            Vector3f vert2 = vertices.get(v2);
//            Vector3f vert3 = vertices.get(v3);
//
//
//            System.out.println("Rasterizing triangle: v1=" + v1 + ", v2=" + v2 + ", v3=" + v3 + ", z values: v1.z=" + vert1.getZ() + ", v2.z=" + vert2.getZ() + ", v3.z=" + vert3.getZ());
//
//            // Проецируем вершины на 2D плоскость
//            float[] projected = projectTriangle(vert1, vert2, vert3);
//
//            // Если проекция не удалась, выводим ошибку и выходим из метода
//            if (projected == null) {
//                System.err.println("Error: NaN in projected coordinates.");
//                return;
//            }
//
//            // Вычисляем ограничивающий прямоугольник для треугольника
//            float minX = Math.min(projected[0], Math.min(projected[2], projected[4]));
//            float maxX = Math.max(projected[0], Math.max(projected[2], projected[4]));
//            float minY = Math.min(projected[1], Math.min(projected[3], projected[5]));
//            float maxY = Math.max(projected[1], Math.max(projected[3], projected[5]));
//
//            // Проходим по всем пикселям в ограничивающем прямоугольнике
//            for (int y = (int) Math.floor(minY); y <= (int) Math.floor(maxY); y++) {
//                for (int x = (int) Math.floor(minX); x <= (int) Math.floor(maxX); x++) {
//                    // Проверяем, находится ли пиксель в пределах изображения
//                    if (x < 0 || x >= width || y < 0 || y >= height) {
//                        continue;
//                    }
//
//                    // Вычисляем барицентрические координаты для текущего пикселя
//                    float[] barycentric = barycentricCoordinates(x, y, projected);
//
//                    // Проверяем, находится ли пиксель внутри треугольника
//                    if (barycentric[0] < 0 || barycentric[1] < 0 || barycentric[2] < 0) {
//                        continue;
//                    }
//
//                    // Вычисляем z-координату для текущего пикселя
//                    float z = vert1.getZ() * barycentric[0] + vert2.getZ() * barycentric[1] + vert3.getZ() * barycentric[2];
//
//                    // Если z-координата больше, чем текущее значение в Z-буфере, то отрисовываем пиксель
//                    if (z > zBuffer[x][y]) {
//                        zBuffer[x][y] = z; // Обновляем значение Z-буфера
//
//                        // Получаем нормаль для текущего пикселя
//                        Vector3f normal = modelTriangulator.getNormal(
//                                new Vector3f(0, 0, v1),
//                                new Vector3f(0, 0, v2),
//                                new Vector3f(0, 0, v3),
//                                barycentric, vertices);
//                        // Если нормаль существует
//                        if (normal.length() != 0) {
//                            float diffuseIntensity = Math.max(0, normal.dot(lightDirection)); // Рассчитываем интенсивность освещения
//                            int colorValue = getColor(diffuseIntensity, color); // Получаем цвет пикселя
//                            image.setRGB(x, y, colorValue); // Отрисовываем пиксель на изображении
//                        }
//
//                        if (texture != null && !textureVertices.isEmpty()) {
//                            colorValue = getTextureColor(texture, textureVertices, triangle, barycentric);
//                        } else {
//                            image.setRGB(x, y, colorValue);
//                        }
//                    }
//                }
//            }
//            System.out.println("Rasterizing triangle finished: v1=" + v1 + ", v2=" + v2 + ", v3=" + v3);
//        }
//    /*todo: ВАЖНО! этот метод getTextureColor
//        предполагает что textureVertices - список Vector2f объектов,
//        соответствующих текстурным координатами вершин
//        Индексы:
//                triangle.v1
//                triangle.v2
//                triangle.v3
//        корректные индексы в textureVertices
//
//     */
//
//        public int getTextureColor(BufferedImage texture, List<Vector2f> textureVertices, ModelTriangulator.Triangle triangle, float[] barycentric) {
//
//            int t1 = triangle.v1;
//            int t2 = triangle.v2;
//            int t3 = triangle.v3;
//            Vector2f uv1 = textureVertices.get(t1);
//            Vector2f uv2 = textureVertices.get(t2);
//            Vector2f uv3 = textureVertices.get(t3);
//
//            if (uv1 == null || uv2 == null || uv3 == null) {
//                return 0;
//            }
//
//            float u = uv1.getX() * barycentric[0] + uv2.getX() * barycentric[1] + uv3.getX() * barycentric[2];
//            float v = uv1.getY() * barycentric[0] + uv2.getY() * barycentric[1] + uv3.getY() * barycentric[2];
//            //зафиксировать координаты, важно не забывать
//
//            u = Math.max(0, Math.min(u, 1f));
//            v = Math.max(0, Math.min(v, 1f));
//
//            int x = Math.min(texture.getWidth() - 1, Math.max(0, (int) (u * texture.getWidth())));
//            int y = Math.min(texture.getHeight() - 1, Math.max(0, (int) (v * texture.getHeight())));
//
//            return texture.getRGB(x, y);
//        }
//
//        /**
//         * Метод для проецирования 3D координат треугольника в 2D
//         *
//         * @param v1 Первая вершина треугольника
//         * @param v2 Вторая вершина треугольника
//         * @param v3 Третья вершина треугольника
//         * @id865676420 (@ return) Массив из 6 float чисел (x1, y1, x2, y2, x3, y3) координат
//         */
//
//        private float[] projectTriangle(Vector3f v1, Vector3f v2, Vector3f v3) {
//            float fov = (float) Math.toRadians(camera.getFov()); // Получаем угол обзора камеры и переводим в радианы
//            float aspectRatio = (float) width / height; // Вычисляем соотношение сторон экрана
//            float scale = 1 / (float) Math.tan(fov / 2); // Вычисляем масштабный коэффициент
//            float[] projected = new float[6]; // Массив для хранения 2D координат
//
//            float z1 = v1.getZ(); // z-координата первой вершины
//            float z2 = v2.getZ(); // z-координата второй вершины
//            float z3 = v3.getZ(); // z-координата третьей вершины
//
//            // Проверяем, чтобы z-координаты вершин были положительными, т.к.деление на ноль или отрицательное число приведет к ошибке
//            if (z1 <= 0 || z2 <= 0 || z3 <= 0) {
//                System.err.println("Error: z values are <= 0 in projectTriangle: z1=" + z1 + ", z2=" + z2 + ", z3=" + z3);
//                return null; // Если условие не выполняется, то возвращаем null
//            }
//
//            // Вычисляем 2D координаты
//            projected[0] = (v1.getX() * scale * aspectRatio) / z1 * (width / 2) + (width / 2);
//            projected[1] = (v1.getY() * scale) / z1 * (height / 2) + (height / 2);
//            projected[2] = (v2.getX() * scale * aspectRatio) / z2 * (width / 2) + (width / 2);
//            projected[3] = (v2.getY() * scale) / z2 * (height / 2) + (height / 2);
//            projected[4] = (v3.getX() * scale * aspectRatio) / z3 * (width / 2) + (width / 2);
//            projected[5] = (v3.getY() * scale) / z3 * (height / 2) + (height / 2);
//            return projected; // Возвращаем массив с 2D координатами
//        }
//
//        /**
//         * Метод для вычисления барицентрических координат
//         *
//         * @param x         Координата x пикселя
//         * @param y         Координата y пикселя
//         * @param projected Массив с 2D координатами вершин треугольника
//         * @id865676420 (@ return) Массив барицентрических координат (w1, w2, w3)
//         */
//        private float[] barycentricCoordinates(float x, float y, float[] projected) {
//            float x1 = projected[0]; // x-координата первой вершины
//            float y1 = projected[1]; // y-координата первой вершины
//            float x2 = projected[2]; // x-координата второй вершины
//            float y2 = projected[3]; // y-координата второй вершины
//            float x3 = projected[4]; // x-координата третьей вершины
//            float y3 = projected[5]; // y-координата третьей вершины
//            float area = 0.5f * ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3)); // Вычисляем площадь треугольника
//            float w1 = 0.5f * ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) / area; // Вычисляем барицентрическую координату w1
//            float w2 = 0.5f * ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) / area; // Вычисляем барицентрическую координату w2
//            float w3 = 1 - w1 - w2; // Вычисляем барицентрическую координату w3
//            return new float[]{w1, w2, w3}; // Возвращаем массив с барицентрическими координатами
//        }
//
//        /**
//         * Метод для получения цвета пикселя.
//         *
//         * @param diffuseIntensity Интенсивность освещения
//         * @param color            Цвет заполнения полигона
//         * @id865676420 (@ return) цвет пикселя в формате int
//         */
//
//        public int getColor(float diffuseIntensity, Color color) {
//            float r = (float) (color.getRed() * diffuseIntensity); // Вычисляем красный канал цвета
//            float g = (float) (color.getGreen() * diffuseIntensity); // Вычисляем зеленый канал цвета
//            float b = (float) (color.getBlue() * diffuseIntensity); // Вычисляем синий канал цвета
//            r = Math.min(255, Math.max(0, r)); // Ограничиваем красный канал значением от 0 до 255
//            g = Math.min(255, Math.max(0, g)); // Ограничиваем зеленый канал значением от 0 до 255
//            b = Math.min(255, Math.max(0, b)); // Ограничиваем синий канал значением от 0 до 255
//
//            java.awt.Color color1 = new java.awt.Color(r, g, b);
//
//            return color1.getRGB();  // Возвращаем цвет в формате int
//            //return 0;
//        }
//    }


