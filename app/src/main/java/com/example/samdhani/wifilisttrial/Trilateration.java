package com.example.samdhani.wifilisttrial;

import java.util.Scanner;

public class Trilateration {
    private static double  MAXZERO = 0.0;
    static Vector3D result3 = new Vector3D();
    static Vector3D result4 = new Vector3D();

    /* Return the difference of two vectors, (vector1 - vector2). */
    private static Vector3D vdiff(Vector3D vector1, Vector3D vector2){
        Vector3D v = new Vector3D();
        v.x = vector1.x - vector2.x;
        v.y = vector1.y - vector2.y;
        v.z = vector1.z - vector2.z;
        return v;
    }

    /* Return the sum of two vectors. */
    private static Vector3D vsum(Vector3D vector1,Vector3D vector2){
        Vector3D v = new Vector3D();
        v.x = vector1.x + vector2.x;
        v.y = vector1.y + vector2.y;
        v.z = vector1.z + vector2.z;
        return v;

    }

    /* Multiply vector by a number. */
    private static Vector3D vmul(Vector3D vector,double n){
        Vector3D v = new Vector3D();
        v.x = vector.x * n;
        v.y = vector.y * n;
        v.z = vector.z * n;
        return v;
    }

    /* Divide vector by a number. */
    private static Vector3D vdiv(Vector3D vector,double n){
        Vector3D v = new Vector3D();
        v.x = vector.x / n;
        v.y = vector.y / n;
        v.z = vector.z / n;
        return v;
    }

    /* Return the Euclidean norm. */
    private static double vnorm(Vector3D vector){
        return Math.sqrt(vector.x * vector.x + vector.y * vector.y + vector.z * vector.z);
    }
    /* Return the dot product of two vectors. */
    private static double dot(Vector3D vector1, Vector3D vector2){
        return vector1.x * vector2.x + vector1.y * vector2.y + vector1.z * vector2.z;
    }

    /* Replace vector with its cross product with another vector. */
    private static Vector3D cross(Vector3D vector1, Vector3D vector2){
        Vector3D v = new Vector3D();
        v.x = vector1.y * vector2.z - vector1.z * vector2.y;
        v.y = vector1.z * vector2.x - vector1.x * vector2.z;
        v.z = vector1.x * vector2.y - vector1.y * vector2.x;
        return v;
    }

    /* Return zero if successful, negative error otherwise.
     * The last parameter is the largest nonnegative number considered zero;
     * it is somewhat analoguous to machine epsilon (but inclusive).
    */
    private static int trilateration(Vector3D result1, Vector3D result2,
                                     Vector3D p1, double r1,
                                     Vector3D p2, double r2,
                                     Vector3D p3, double r3,double maxzero)
    {
        Vector3D ex, ey, ez, t1, t2;
        double	h, i, j, x, y, z, t;

	/* h = |p2 - p1|, ex = (p2 - p1) / |p2 - p1| */
        ex = vdiff(p2, p1);
        h = vnorm(ex);
        if (h <= maxzero) {
		/* p1 and p2 are concentric. */
            return -1;
        }
        ex = vdiv(ex, h);

	/* t1 = p3 - p1, t2 = ex (ex . (p3 - p1)) */
        t1 = vdiff(p3, p1);
        i = dot(ex, t1);
        t2 = vmul(ex, i);

	/* ey = (t1 - t2), t = |t1 - t2| */
        ey = vdiff(t1, t2);
        t = vnorm(ey);
        if (t > maxzero) {
		/* ey = (t1 - t2) / |t1 - t2| */
            ey = vdiv(ey, t);

		/* j = ey . (p3 - p1) */
            j = dot(ey, t1);
        } else
            j = 0.0;

	/* Note: t <= maxzero implies j = 0.0. */
        if (Math.abs(j) <= maxzero) {
		/* p1, p2 and p3 are colinear. */

		/* Is point p1 + (r1 along the axis) the intersection? */
            t2 = vsum(p1, vmul(ex, r1));
            if (Math.abs(vnorm(vdiff(p2, t2)) - r2) <= maxzero &&
                    Math.abs(vnorm(vdiff(p3, t2)) - r3) <= maxzero) {
			/* Yes, t2 is the only intersection point. */
                if (result1!=null)
                    result3 = t2;
                if (result2!=null)
                    result4 = t2;
                return 0;
            }

		/* Is point p1 - (r1 along the axis) the intersection? */
            t2 = vsum(p1, vmul(ex, -r1));
            if (Math.abs(vnorm(vdiff(p2, t2)) - r2) <= maxzero &&
                    Math.abs(vnorm(vdiff(p3, t2)) - r3) <= maxzero) {
			/* Yes, t2 is the only intersection point. */
                if (result1!=null)
                    result3 = t2;
                if (result2!=null)
                    result4 = t2;
                return 0;
            }

            return -2;
        }

	/* ez = ex x ey */
        ez = cross(ex, ey);

        x = (r1*r1 - r2*r2) / (2*h) + h / 2;
        y = (r1*r1 - r3*r3 + i*i) / (2*j) + j / 2 - x * i / j;
        z = r1*r1 - x*x - y*y;
        if (z < -maxzero) {
		/* The solution is invalid. */
            return -3;
        } else
        if (z > 0.0)
            z = Math.sqrt(z);
        else
            z = 0.0;

	/* t2 = p1 + x ex + y ey */
        t2 = vsum(p1, vmul(ex, x));
        t2 = vsum(t2, vmul(ey, y));

	/* result1 = p1 + x ex + y ey + z ez */
        if (result1!=null)
            result3 = vsum(t2, vmul(ez, z));

	/* result1 = p1 + x ex + y ey - z ez */
        if (result2!=null)
            result4 = vsum(t2, vmul(ez, -z));

        return 0;
    }

    public static Vector3D myTrilateration(double x1,double y1,double x2,double y2,double x3,double y3,double d1,double d2,double d3){
        Vector3D p1 = new Vector3D();
        Vector3D p2 = new Vector3D();
        Vector3D p3 = new Vector3D();
        Vector3D o1 = new Vector3D();
        Vector3D o2 = new Vector3D();
        double	r1, r2, r3;
        int	result = 0;
        Scanner myScan = new Scanner(System.in);
        p1.x = x1;
        p1.y = y1;
        p1.z = 0;
        r1 = d1;
        p2.x = x2;
        p2.y = y2;
        p2.z = 0;
        r2 = myScan.nextDouble();
        p3.x = x3;
        p3.y = y3;
        p3.z = 0;
        r3 = d3;
        result = trilateration(o1, o2, p1, r1, p2, r2, p3, r3, MAXZERO);
        if (result!=0) {
            System.out.printf("No solution (%d).\n", result);
            return null;
        }
        else
            return result3;
    }
}

