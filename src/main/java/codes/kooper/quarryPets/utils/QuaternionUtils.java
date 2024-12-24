package codes.kooper.quarryPets.utils;

import com.github.retrooper.packetevents.util.Quaternion4f;

public class QuaternionUtils {

    // Create a Quaternion4f from yaw and pitch
    public static Quaternion4f fromYawPitch(float yaw, float pitch) {
        float halfYaw = (float) Math.toRadians(yaw) / 2;
        float halfPitch = (float) Math.toRadians(pitch) / 2;

        float cy = (float) Math.cos(halfYaw);
        float sy = (float) Math.sin(halfYaw);
        float cp = (float) Math.cos(halfPitch);
        float sp = (float) Math.sin(halfPitch);

        float w = cy * cp;
        float x = sp;
        float y = sy * cp;
        float z = sy * sp;

        return new Quaternion4f(x, y, z, w);
    }

    // Convert a quaternion to a rotation matrix (3x3)
    public static float[][] toRotationMatrix(Quaternion4f quaternion) {
        float x = quaternion.getX();
        float y = quaternion.getY();
        float z = quaternion.getZ();
        float w = quaternion.getW();

        float[][] matrix = new float[3][3];
        matrix[0][0] = 1 - 2 * y * y - 2 * z * z;
        matrix[0][1] = 2 * x * y - 2 * w * z;
        matrix[0][2] = 2 * x * z + 2 * w * y;

        matrix[1][0] = 2 * x * y + 2 * w * z;
        matrix[1][1] = 1 - 2 * x * x - 2 * z * z;
        matrix[1][2] = 2 * y * z - 2 * w * x;

        matrix[2][0] = 2 * x * z - 2 * w * y;
        matrix[2][1] = 2 * y * z + 2 * w * x;
        matrix[2][2] = 1 - 2 * x * x - 2 * y * y;

        return matrix;
    }

    // Multiply two quaternions
    public static Quaternion4f multiply(Quaternion4f q1, Quaternion4f q2) {
        float x1 = q1.getX();
        float y1 = q1.getY();
        float z1 = q1.getZ();
        float w1 = q1.getW();

        float x2 = q2.getX();
        float y2 = q2.getY();
        float z2 = q2.getZ();
        float w2 = q2.getW();

        float x = w1 * x2 + x1 * w2 + y1 * z2 - z1 * y2;
        float y = w1 * y2 - x1 * z2 + y1 * w2 + z1 * x2;
        float z = w1 * z2 + x1 * y2 - y1 * x2 + z1 * w2;
        float w = w1 * w2 - x1 * x2 - y1 * y2 - z1 * z2;

        return new Quaternion4f(x, y, z, w);
    }
}
