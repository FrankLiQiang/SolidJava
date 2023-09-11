#include <jni.h>
#include <cstring>
#include <cmath>
#include <android/log.h>
#include <vector>
#include <cfloat>
#include <cstdlib>
#include <complex>

using namespace std;

#define LOG_TAG "System.out"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static float eye_x, eye_y, eye_z, DepthZ, CenterX, CenterY, CenterZ;
static float HPI, W2PI, W0, R, r, rr, FirstR, fu, fv;
static int width, height, width00, height00;
static double factorA = 0, factorB = 0, factorC = 0, factorD = 0;
static double f4a = 0, f4b = 0, f4c = 0, f4a2 = 0, f4b2 = 0, f4c2 = 0, f4d = 0, f4e = 0, f4m = 0, f4n = 0, f4p = 0;

static int whSize0[7][2];

const double EPSILON = 0.0000000001;

// 3D vector
struct Vector3d {
public:
    Vector3d() {
    }

    ~Vector3d() {
    }

    Vector3d(double dx, double dy, double dz) {
        x = dx;
        y = dy;
        z = dz;
    }

    // 矢量赋值
    void set(double dx, double dy, double dz) {
        x = dx;
        y = dy;
        z = dz;
    }

    // 矢量相加
    Vector3d operator+(const Vector3d &v) const {
        return Vector3d(x + v.x, y + v.y, z + v.z);
    }

    // 矢量相减
    Vector3d operator-(const Vector3d &v) const {
        return Vector3d(x - v.x, y - v.y, z - v.z);
    }

    //矢量数乘
    Vector3d Scalar(double c) const {
        return Vector3d(c * x, c * y, c * z);
    }

    // 矢量点积
    double Dot(const Vector3d &v) const {
        return x * v.x + y * v.y + z * v.z;
    }

    // 矢量叉积
    Vector3d Cross(const Vector3d &v) const {
        return Vector3d(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
    }

    bool operator==(const Vector3d &v) const {
        if (abs(x - v.x) < EPSILON && abs(y - v.y) < EPSILON && abs(z - v.z) < EPSILON) {
            return true;
        }
        return false;
    }

    double x, y, z;
};

static Vector3d Center, O;
static Vector3d myMap[3000][1500];

/******************************************************************************\
对一个复数 x 开 n 次方
注意：
1、不要使用 std::pow 计算复数开方——复数的虚部为零，实部为负数，计算会失败
2、std::sqrt 计算复数开平方，似乎是没有问题的
\******************************************************************************/
std::complex<double> sqrtn(const std::complex<double>&x,double n)
{
    double  r   =   hypot(x.real(),x.imag());  //模
    if(r > 0.0)
    {
        double  a   =   atan2(x.imag(),x.real());   //辐角

        n   =   1.0 / n;
        r   =   pow(r,n);
        a  *=   n;
        return std::complex<double>(r * cos(a),r * sin(a));
    }
    return std::complex<double>();
}

double      m_z[10];    //方程的系数
double      m_x[12];    //方程的根
/******************************************************************************\
比较方程根的大小
\******************************************************************************/
static int __cdecl CompareX(const void*elem1,const void*elem2)
{
    double d1   =   ((const double*)elem1)[2];
    double d2   =   ((const double*)elem2)[2];
    if(d1 < d2)
    {
        return -1;
    }
    if(d1 > d2)
    {
        return 1;
    }
    return 0;
}

/******************************************************************************\
 * https://github.com/hanford77/SolveEquation/blob/master/vc/dllSDK/src/Solve.cpp
求解一元四次方程
z   [in]    方程系数
            z[0],z[1] 表示 0 次项系数的实部、虚部
            z[2],z[3] 表示 1 次项系数的实部、虚部
            z[4],z[5] 表示 2 次项系数的实部、虚部
            z[6],z[7] 表示 3 次项系数的实部、虚部
            z[8],z[9] 表示 4 次项系数的实部、虚部
x   [out]   求解出来的根
            x[0],x[ 1],x[ 2] 第 1 个根的实部、虚部、误差值
            x[3],x[ 4],x[ 5] 第 2 个根的实部、虚部、误差值
            x[6],x[ 7],x[ 8] 第 3 个根的实部、虚部、误差值
            x[9],x[10],x[11] 第 4 个根的实部、虚部、误差值
返回：根的个数，范围 [0,4]
\******************************************************************************/
int __stdcall SolveEquation(const double z[10],double x[12])
{
    if(NULL == z || NULL == x)
    {//参数无效
        return 0;
    }
    std::complex<double>    xc[4];          //求出的复数根，最多 4 个
    int                     nCount  =   0;  //根的个数
    int                     nPower  =   0;  //最高次数
    if(fabs(z[8]) > DBL_MIN || fabs(z[9]) > DBL_MIN)
    {//一元四次方程，计算公式采用《一元四次方程-16.04.05.pdf》公式(28)至(35)
        nPower  =   4;  //最高次数
        const std::complex<double>  a(z[8],z[9]);
        const std::complex<double>  b(z[6],z[7]);
        const std::complex<double>  c(z[4],z[5]);
        const std::complex<double>  d(z[2],z[3]);
        const std::complex<double>  e(z[0],z[1]);
        std::complex<double>        P   =   (c * c + 12.0 * a * e - 3.0 * b * d) * (1.0 / 9.0);
        std::complex<double>        Q   =   (27.0 * a * d * d + 2.0 * c * c * c + 27.0 * b * b * e - 72.0 * a * c *e - 9.0 * b * c * d) * (1.0 / 54.0);
        std::complex<double>        D   =   sqrtn(Q * Q - P * P * P,2.0);
        std::complex<double>        u   =   sqrtn(Q + D,3.0);
        std::complex<double>        v   =   sqrtn(Q - D,3.0);
        if(v.real() * v.real() + v.imag() * v.imag() > u.real() * u.real() + u.imag() * u.imag())
        {//v 的模较大
            u   =   v;
        }
        if(fabs(u.real()) > DBL_MIN || fabs(u.imag()) > DBL_MIN)
        {//u 不为零
            v   =   P / u;
        }
        else
        {//u 为零则 v 也取值为零
            u   =
            v   =   0.0;
        }
        std::complex<double>    m;
        std::complex<double>    S   =   b * b - (8.0 / 3.0) * a * c;
        std::complex<double>    T   =   4.0 * a;
        {//计算 m,S,T
            std::complex<double>    o1(-0.5,+0.86602540378443864676372317075294);//ω
            std::complex<double>    o2(-0.5,-0.86602540378443864676372317075294);//ω*ω
            u  *=   T;
            v  *=   T;
            std::complex<double>    t[3]    =
                    {
                            u +      v,
                            o1 * u + o2 * v,
                            o2 * u + o1 * v,
                    };
            double  dMod2   =   0.0;    //模的平方
            double  dMax2   =   0.0;    //模平方的最大值
            int     iMax    =   -1;
            for(int i = 0;i < 3;++i)
            {
                T       =   S + t[i];
                dMod2   =   T.real() * T.real() + T.imag() * T.imag();
                if(iMax < 0 || dMax2 < dMod2)
                {
                    dMax2   =   dMod2;
                    iMax    =   i;
                }
            }
            if(dMax2 > DBL_MIN)
            {
                m   =   sqrtn(S + t[iMax],2.0);
                S   =   2.0 * b * b - (16.0 / 3.0) * a * c - t[iMax];
                T   =   (8.0 * a * b * c - 16.0 * a * a * d - 2.0 * b * b * b) / m;
            }
            else
            {
                m   =
                T   =   0.0;
            }
        }
        v       =   (1.0 / 4.0) / a;
        u       =   sqrtn(S - T,2.0);
        xc[0]   =   (-b - m + u) * v;
        xc[1]   =   (-b - m - u) * v;
        u       =   sqrtn(S + T,2.0);
        xc[2]   =   (-b + m + u) * v;
        xc[3]   =   (-b + m - u) * v;
        nCount  =   4;
    }
    else if(fabs(z[6]) > DBL_MIN || fabs(z[7]) > DBL_MIN)
    {//一元三次方程，计算公式采用《一元三次方程-16.04.06.pdf》公式(29)至(34)
        nPower  =   3;  //最高次数
        const std::complex<double>  a(z[6],z[7]);
        const std::complex<double>  b(z[4],z[5]);
        const std::complex<double>  c(z[2],z[3]);
        const std::complex<double>  d(z[0],z[1]);
        std::complex<double>        P   =   4.0 * (b * b - 3.0 * a * c);
        std::complex<double>        Q   =   4.0 * (9.0 * a * b * c - 27.0 * a * a * d - 2.0 * b * b * b);
        std::complex<double>        D   =   sqrtn(Q * Q - P * P * P,2.0);
        std::complex<double>        u   =   sqrtn(Q + D,3.0);
        std::complex<double>        v   =   sqrtn(Q - D,3.0);
        std::complex<double>        o1(-0.5,+0.86602540378443864676372317075294);//ω
        std::complex<double>        o2(-0.5,-0.86602540378443864676372317075294);//ω*ω
        if(v.real() * v.real() + v.imag() * v.imag() > u.real() * u.real() + u.imag() * u.imag())
        {//v 的模较大
            u   =   v;
        }
        if(fabs(u.real()) > DBL_MIN || fabs(u.imag()) > DBL_MIN)
        {//u 不为零
            v   =   P / u;
        }
        else
        {//u 为零则 v 也取值为零
            u   =
            v   =   0.0;
        }
        D       =   (1.0 / 6.0) / a;
        xc[0]   =   (     u +      v - 2.0 * b) * D;
        xc[1]   =   (o1 * u + o2 * v - 2.0 * b) * D;
        xc[2]   =   (o2 * u + o1 * v - 2.0 * b) * D;
        nCount  =   3;
    }
    else if(fabs(z[4]) > DBL_MIN || fabs(z[5]) > DBL_MIN)
    {//一元二次方程
        nPower  =   2;  //最高次数
        const std::complex<double>  a(z[4],z[5]);
        const std::complex<double>  b(z[2],z[3]);
        const std::complex<double>  c(z[0],z[1]);
        std::complex<double>        D   =   sqrtn(b * b - 4.0 * a * c,2.0);
        std::complex<double>        t   =   (-1.0 / 2.0) / a;

        xc[0]   =   (b + D) * t;
        xc[1]   =   (b - D) * t;
        nCount  =   2;
    }
    else if(fabs(z[2]) > DBL_MIN || fabs(z[3]) > DBL_MIN)
    {//一元一次方程
        nPower  =   1;  //最高次数
        const std::complex<double>  a(z[2],z[3]);
        const std::complex<double>  b(z[0],z[1]);
        xc[0]   =  -b / a;
        nCount  =   1;
    }
    if(nCount > 0)
    {//删除无效的根
        int nDel = 0;   //删除的个数
        for(int i = 0;i < nCount;++i)
        {
            if(finite(xc[i].real()) && finite(xc[i].imag()))
            {//此根有效
                if(nDel)
                {
                    xc[i - nDel]    =   xc[i];
                }
            }
            else
            {//此根无效
                ++nDel;
            }
        }
        nCount -=   nDel;
        if(nCount > 0)
        {
            const double    PI  =   3.1415926535897932384626433832795;
            int p   =   0;
            for(int i = 0;i < nCount;++i)
            {//将 xc 填入 x
                x[p + 2]    =   fabs(atan2(x[p + 1] = xc[i].imag(),x[p] = xc[i].real()));
                if(x[p + 2] > 0.5 * PI)
                {
                    x[p + 2]    =   PI - x[p + 2];
                }
                p  +=   3;
            }
            if(nCount > 1)
            {//排序
                qsort(x,nCount,3 * sizeof(double),CompareX);
            }
            {//计算误差
                std::complex<double> t;
                int k   =   0;
                int p   =   0;
                for(int i = 0;i < nCount;++i)
                {
                    t   =   0.0;
                    for(k = 0;k <= nPower;++k)
                    {
                        t   =   t * std::complex<double>(x[p],x[p + 1])
                                +   std::complex<double>(z[2 * (nPower - k)],z[2 * (nPower - k) + 1]);
                    }
                    x[p + 2]=   hypot(t.real(),t.imag());
                    p      +=   3;
                }
            }
        }
    }
    return nCount;
}

//求解一元二次方程组ax*x + b*x + c = 0
void SolvingQuadratics(double a, double b, double c, vector<double> &t) {
    double delta = b * b - 4 * a * c;

    if (delta < 0) {
        return;
    }

    if (abs(delta) < EPSILON) {
        t.push_back(-b / (2 * a));
    } else {
        t.push_back((-b + sqrt(delta)) / (2 * a));
        t.push_back((-b - sqrt(delta)) / (2 * a));
    }
}

void LineIntersectSphere(Vector3d &O, Vector3d &E, double R, vector<Vector3d> &points) {
    Vector3d D = E - O;            //线段方向向量

    double a = (D.x * D.x) + (D.y * D.y) + (D.z * D.z);
    double b = (2 * D.x * (O.x - Center.x) + 2 * D.y * (O.y - Center.y) +
                2 * D.z * (O.z - Center.z));
    double c = ((O.x - Center.x) * (O.x - Center.x) + (O.y - Center.y) * (O.y - Center.y) +
                (O.z - Center.z) * (O.z - Center.z)) - R * R;

    vector<double> t;
    SolvingQuadratics(a, b, c, t);

    for (auto it : t) {
        points.push_back(O + D.Scalar(it));
    }
}

double getDistance2(float x1, float y1, float x2, float y2) {
    return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
}

double getDistance32(Vector3d &P1, Vector3d &P2) {
    double ret = (P1.x - P2.x) * (P1.x - P2.x) + (P1.y - P2.y) * (P1.y - P2.y) +
                 +(P1.z - P2.z) * (P1.z - P2.z);
    return ret;
}

Vector3d GetFootOfPerpendicular(const Vector3d &p0, const Vector3d &p1, const Vector3d &p2) {
    double dx = p2.x - p1.x;
    double dy = p2.y - p1.y;
    double dz = p2.z - p1.z;

    double k = ((p1.x - p0.x) * dx + (p1.y - p0.y) * dy + (p1.z - p0.z) * dz)
               / ((dx * dx) + (dy * dy) + (dz * dz)) * -1;

    Vector3d retVal(k * dx + p1.x, k * dy + p1.y, k * dz + p1.z);

    return retVal;
}

void
getPanel(Vector3d &p1, Vector3d &p2, Vector3d &p3, double &a, double &b, double &c, double &d) {
    a = ((p2.y - p1.y) * (p3.z - p1.z) - (p2.z - p1.z) * (p3.y - p1.y));
    b = ((p2.z - p1.z) * (p3.x - p1.x) - (p2.x - p1.x) * (p3.z - p1.z));
    c = ((p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x));
    d = (0 - (a * p1.x + b * p1.y + c * p1.z));
}

bool isBack(Vector3d &p) {
    return factorA * p.x + factorB * p.y + factorC * p.z + factorD > 0;
}

double getLongitude(Vector3d &P, Vector3d &A, Vector3d &M) {
    //点P 到 北极，球心连线的 垂足
    Vector3d foot = GetFootOfPerpendicular(P, A, Center);
    Vector3d a = P - foot;            // 垂足到点P的方向向量
    Vector3d b = M - Center;        // 本初子午线与赤道的交点与地心的方向向量

    double c = (a.x * b.x + a.y * b.y + a.z * b.z) / sqrt(a.x * a.x + a.y * a.y + a.z * a.z)
               / sqrt(b.x * b.x + b.y * b.y + b.z * b.z);

    if (isBack(P)) {
        return 2 * 3.14159f - acos(c);
    }
    return acos(c);
}

extern "C"
JNIEXPORT void JNICALL Java_com_frank_solid_BallActivity_Initialization(JNIEnv *env, jclass obj,
                                                                        const jfloat e_x,
                                                                        const jfloat e_y,
                                                                        const jfloat e_z,
                                                                        const jfloat Z,
                                                                        const jfloat cx,
                                                                        const jfloat cy,
                                                                        const jfloat cz) {
    eye_x = e_x;
    eye_y = e_y;
    eye_z = e_z;
    DepthZ = Z;
    CenterX = cx;
    CenterY = cy;
    CenterZ = cz;
    Center.set(CenterX, CenterY, CenterZ);
    O.set(eye_x, eye_y, eye_z);
}

extern "C"
JNIEXPORT void JNICALL Java_com_frank_solid_BallActivity_Initialization2(JNIEnv *env, jclass obj,
                                                                         const jint width0,
                                                                         const jint height0,
                                                                         const jint width1,
                                                                         const jint height1,
                                                                         const jfloat R0,
                                                                         const jfloat r0) {
    HPI = (double) height0 / 3.14159f;
    W0 = width0;
    W2PI = (double) width0 / 3.14159f / 2.0f;
    R = R0, r = r0, FirstR = r;
    width = width1;
    height = height1;

    for (int y = 0; y < height; y++) {
        if (y < height / 2 - r || y > height / 2 + r) {
            continue;
        }
        for (int x = 0; x < width; x++) {
            if (getDistance2(x, y, width / 2, height / 2) < r) {
                Vector3d E(x, y, DepthZ);
                vector<Vector3d> points;
                LineIntersectSphere(O, E, R, points);
                for (auto it : points) {
                    if (it.z >= CenterZ) {
                        Vector3d P(it.x, it.y, it.z);
                        myMap[y][x] = P;
                    }
                }
            }
        }
    }
}

extern "C"
JNIEXPORT jint JNICALL Java_com_frank_solid_BallActivity_transforms(JNIEnv *env, jclass obj,
                                                                    const jbyteArray pSrcData,
                                                                    const jbyteArray pOutData,
                                                                    const jfloat ArcticX,
                                                                    const jfloat ArcticY,
                                                                    const jfloat ArcticZ,
                                                                    const jfloat MeridianX,
                                                                    const jfloat MeridianY,
                                                                    const jfloat MeridianZ,
                                                                    const jfloat r0) {
    jbyte *Buf = env->GetByteArrayElements(pSrcData, 0);
    jbyte *BufOut = env->GetByteArrayElements(pOutData, 0);
    jint bmpLength = env->GetArrayLength(pSrcData);
    jint newBmpLength = env->GetArrayLength(pOutData);

    r = r0;
    double scale = r / FirstR;
    Vector3d Arctic(ArcticX, ArcticY, ArcticZ);
    Vector3d Meridian(MeridianX, MeridianY, MeridianZ);

    getPanel(Arctic, Meridian, Center, factorA, factorB, factorC, factorD);

    double latitude, longitude;
    int original_point, pixel_point, x0, y0;

    for (int y = 0; y < height; y++) {
        if (y < height / 2 - r || y > height / 2 + r) {
            for (int x = 0; x < width; x++) {
                pixel_point = (width * y + x) * 4;
                *(BufOut + pixel_point) = 0;
                *(BufOut + pixel_point + 1) = 0;
                *(BufOut + pixel_point + 2) = 0;
            }
            continue;
        }
        for (int x = 0; x < width; x++) {
            pixel_point = (width * y + x) * 4;
            if (getDistance2(x, y, width / 2, height / 2) < r) {
                Vector3d P = myMap[y][x];
                if (r == FirstR) {
                    P = myMap[y][x];
                } else {
                    x0 = (int) ((double) width / 2.0f +
                                ((double) x - (double) width / 2.0f) / scale);
                    y0 = (int) ((double) height / 2.0f +
                                ((double) y - (double) height / 2.0f) / scale);
                    if (x0 < 0) x0 = 0;
                    if (x0 >= width) x0 = width - 1;
                    if (y0 < 0) y0 = 0;
                    if (y0 >= height) y0 = height - 1;
                    P = myMap[y0][x0];
                }
                latitude = acos(1 - getDistance32(P, Arctic) / (2 * R * R));
                longitude = getLongitude(P, Arctic, Meridian);
                original_point = ((int) (HPI * latitude) * W0 + (int) (W2PI * longitude)) * 4;
                *(BufOut + pixel_point) = *(Buf + original_point);
                *(BufOut + pixel_point + 1) = *(Buf + original_point + 1);
                *(BufOut + pixel_point + 2) = *(Buf + original_point + 2);
            } else {
                *(BufOut + pixel_point) = 0;
                *(BufOut + pixel_point + 1) = 0;
                *(BufOut + pixel_point + 2) = 0;
            }
        }
    }
    env->ReleaseByteArrayElements(pSrcData, Buf, 0);
    env->ReleaseByteArrayElements(pOutData, BufOut, 0);

    return 0;
}

extern "C"
JNIEXPORT void JNICALL Java_com_frank_solid_Common_setEYE(JNIEnv *env, jclass obj,
                                                          const jfloat e_x,
                                                          const jfloat e_y,
                                                          const jfloat e_z) {
    eye_x = e_x;
    eye_y = e_y;
    eye_z = e_z;
    O.set(eye_x, eye_y, eye_z);
}

extern "C"
JNIEXPORT jint JNICALL Java_com_frank_solid_Common_isVisible(JNIEnv *env, jclass obj,
                                                             const jfloat xE,
                                                             const jfloat yE,
                                                             const jfloat zE,
                                                             const jfloat xF,
                                                             const jfloat yF,
                                                             const jfloat zF,
                                                             const jfloat xG,
                                                             const jfloat yG,
                                                             const jfloat zG) {
    Vector3d E(xE, yE, zE);
    Vector3d F(xF, yF, zF);
    Vector3d G(xG, yG, zG);

    Vector3d EF = F - E;
    Vector3d EG = G - E;
    Vector3d N = EF.Cross(EG);
    Vector3d V = O - E;

    if (N.Dot(V) > 0) {
        return 0;
    } else {
        return 1;
    }
}

extern "C"
JNIEXPORT int JNICALL
Java_com_frank_solid_Pict6CubeActivity_Initialization2(JNIEnv *env, jclass obj,
                                                       const jobjectArray whSize) {

    for (int i = 0; i < 7; i++) {
        jintArray testArray = (jintArray) env->GetObjectArrayElement(whSize, i);
        jint *testP = env->GetIntArrayElements(testArray, NULL);
        whSize0[i][0] = *testP;
        whSize0[i][1] = *(testP + 1);
    }

    width = whSize0[6][0];
    height = whSize0[6][1];

    return height;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_frank_solid_PictCubeActivity_Initialization2(JNIEnv *env, jclass obj,
                                                      const jint w, const jint h, const jint w00,
                                                      const jint h00) {
    width = w;
    height = h;
    width00 = w00;
    height00 = h00;
}

struct pt {
    float x, y;
};

struct quat {
    pt points[4];
};

float crossmulti2d(float x1, float y1, float x2, float y2) {
    return x1 * y2 - x2 * y1;
}

bool inquat(quat _q, pt _pt) {
    pt vec1, vec2;
    vec1.x = _q.points[1].x - _q.points[0].x;
    vec1.y = _q.points[1].y - _q.points[0].y;
    vec2.x = _pt.x - _q.points[0].x;
    vec2.y = _pt.y - _q.points[0].y;
    if (crossmulti2d(vec2.x, vec2.y, vec1.x, vec1.y) < 0) {
        return false;
    }
    vec1.x = _q.points[2].x - _q.points[1].x;
    vec1.y = _q.points[2].y - _q.points[1].y;
    vec2.x = _pt.x - _q.points[1].x;
    vec2.y = _pt.y - _q.points[1].y;
    if (crossmulti2d(vec2.x, vec2.y, vec1.x, vec1.y) < 0) {
        return false;
    }
    vec1.x = _q.points[3].x - _q.points[2].x;
    vec1.y = _q.points[3].y - _q.points[2].y;
    vec2.x = _pt.x - _q.points[2].x;
    vec2.y = _pt.y - _q.points[2].y;
    if (crossmulti2d(vec2.x, vec2.y, vec1.x, vec1.y) < 0) {
        return false;
    }
    vec1.x = _q.points[0].x - _q.points[3].x;
    vec1.y = _q.points[0].y - _q.points[3].y;
    vec2.x = _pt.x - _q.points[3].x;
    vec2.y = _pt.y - _q.points[3].y;
    if (crossmulti2d(vec2.x, vec2.y, vec1.x, vec1.y) < 0) {
        return false;
    }
    return true;
}

struct vec2 {
public:
    vec2() {
    }

    ~vec2() {
    }

    vec2(double dx, double dy) {
        x = dx;
        y = dy;
    }

    // 矢量赋值
    void set(double dx, double dy) {
        x = dx;
        y = dy;
    }

    // 矢量相加
    vec2 operator+(const vec2 &v) const {
        return vec2(x + v.x, y + v.y);
    }

    // 矢量相减
    vec2 operator-(const vec2 &v) const {
        return vec2(x - v.x, y - v.y);
    }

    //矢量数乘
    vec2 Scalar(double c) const {
        return vec2(c * x, c * y);
    }

    // 矢量点积
    double Dot(const vec2 &v) const {
        return x * v.x + y * v.y;
    }

    bool operator==(const vec2 &v) const {
        if (abs(x - v.x) < EPSILON && abs(y - v.y) < EPSILON) {
            return true;
        }
        return false;
    }

    double x, y;
};

float cross(vec2 a, vec2 b) { return a.x * b.y - a.y * b.x; }

vec2 invBilinear(vec2 p, vec2 a, vec2 b, vec2 c, vec2 d) {
    vec2 e = b - a;
    vec2 f = d - a;
    vec2 g = a - b + c - d;
    vec2 h = p - a;

    float k2 = cross(g, f);
    float k1 = cross(e, f) + cross(h, g);
    float k0 = cross(h, e);

    float w = k1 * k1 - 4.0 * k0 * k2;
    if (w < 0.0) return vec2(-1, 0);

    w = sqrt(w);

    float v1 = (-k1 - w) / (2.0 * k2);
    float u1 = (h.x - f.x * v1) / (e.x + g.x * v1);

    float v2 = (-k1 + w) / (2.0 * k2);
    float u2 = (h.x - f.x * v2) / (e.x + g.x * v2);

    float u = u1;
    float v = v1;

    if (v < 0.0 || v > 1.0 || u < 0.0 || u > 1.0) {
        u = u2;
        v = v2;
    }
    if (v < 0.0 || v > 1.0 || u < 0.0 || u > 1.0) {
        u = -1.0;
        v = -1.0;
    }

    return vec2(u, v);
}

extern "C"
JNIEXPORT void JNICALL Java_com_frank_solid_PictCubeActivity_transforms(JNIEnv *env, jclass obj,
                                                                        const jint count,
                                                                        const jint index1,
                                                                        const jint index2,
                                                                        const jint index3,
                                                                        const jbyteArray pSrcData,
                                                                        const jbyteArray pOutData,
                                                                        const jfloat A1x,
                                                                        const jfloat A1y,
                                                                        const jfloat B1x,
                                                                        const jfloat B1y,
                                                                        const jfloat C1x,
                                                                        const jfloat C1y,
                                                                        const jfloat D1x,
                                                                        const jfloat D1y,
                                                                        const jfloat A2x,
                                                                        const jfloat A2y,
                                                                        const jfloat B2x,
                                                                        const jfloat B2y,
                                                                        const jfloat C2x,
                                                                        const jfloat C2y,
                                                                        const jfloat D2x,
                                                                        const jfloat D2y,
                                                                        const jfloat A3x,
                                                                        const jfloat A3y,
                                                                        const jfloat B3x,
                                                                        const jfloat B3y,
                                                                        const jfloat C3x,
                                                                        const jfloat C3y,
                                                                        const jfloat D3x,
                                                                        const jfloat D3y) {

    jbyte *Buf = env->GetByteArrayElements(pSrcData, 0);
    jbyte *BufOut = env->GetByteArrayElements(pOutData, 0);

    int original_point, pixel_point, X1, Y1;

    quat shape1 = {{{A1x, A1y}, {B1x, B1y}, {C1x, C1y}, {D1x, D1y}}};
    quat shape2 = {{{A2x, A2y}, {B2x, B2y}, {C2x, C2y}, {D2x, D2y}}};
    quat shape3 = {{{A3x, A3y}, {B3x, B3y}, {C3x, C3y}, {D3x, D3y}}};

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            pixel_point = (width * y + x) * 4;
            pt tests;
            tests.x = x;
            tests.y = y;
            if (inquat(shape1, tests)) {
                vec2 p(x, y);
                vec2 b(A1x, A1y);
                vec2 a(B1x, B1y);
                vec2 d(C1x, C1y);
                vec2 c(D1x, D1y);

                vec2 v2 = invBilinear(p, a, b, c, d);
                X1 = (int) ((float) width00 * v2.x);
                Y1 = (int) ((float) height00 * v2.y);

                original_point = (Y1 * width00 + X1) * 4;
                *(BufOut + pixel_point) = *(Buf + original_point);
                *(BufOut + pixel_point + 1) = *(Buf + original_point + 1);
                *(BufOut + pixel_point + 2) = *(Buf + original_point + 2);
            } else if (count > 1 && inquat(shape2, tests)) {
                vec2 p(x, y);
                vec2 b(A2x, A2y);
                vec2 a(B2x, B2y);
                vec2 d(C2x, C2y);
                vec2 c(D2x, D2y);

                vec2 v2 = invBilinear(p, a, b, c, d);
                X1 = (int) ((float) width00 * v2.x);
                Y1 = (int) ((float) height00 * v2.y);

                original_point = (Y1 * width00 + X1) * 4;
                *(BufOut + pixel_point) = *(Buf + original_point);
                *(BufOut + pixel_point + 1) = *(Buf + original_point + 1);
                *(BufOut + pixel_point + 2) = *(Buf + original_point + 2);
            } else if (count > 2 && inquat(shape3, tests)) {
                vec2 p(x, y);
                vec2 a(A3x, A3y);
                vec2 d(B3x, B3y);
                vec2 c(C3x, C3y);
                vec2 b(D3x, D3y);

                vec2 v2 = invBilinear(p, a, b, c, d);
                X1 = (int) ((float) width00 * v2.x);
                Y1 = (int) ((float) height00 * v2.y);

                original_point = (Y1 * width00 + X1) * 4;
                *(BufOut + pixel_point) = *(Buf + original_point);
                *(BufOut + pixel_point + 1) = *(Buf + original_point + 1);
                *(BufOut + pixel_point + 2) = *(Buf + original_point + 2);
            } else {
                *(BufOut + pixel_point) = 0;
                *(BufOut + pixel_point + 1) = 0;
                *(BufOut + pixel_point + 2) = 0;
            }
        }
    }

    env->ReleaseByteArrayElements(pSrcData, Buf, 0);

    env->ReleaseByteArrayElements(pOutData, BufOut, 0);
}

extern "C"
JNIEXPORT void JNICALL Java_com_frank_solid_Pict6CubeActivity_transforms(JNIEnv *env, jclass obj,
                                                                         const jint count,
                                                                         const jint index1,
                                                                         const jint index2,
                                                                         const jint index3,
                                                                         const jbyteArray pSrcData1,
                                                                         const jbyteArray pSrcData2,
                                                                         const jbyteArray pSrcData3,
                                                                         const jbyteArray pOutData,
                                                                         const jfloat A1x,
                                                                         const jfloat A1y,
                                                                         const jfloat B1x,
                                                                         const jfloat B1y,
                                                                         const jfloat C1x,
                                                                         const jfloat C1y,
                                                                         const jfloat D1x,
                                                                         const jfloat D1y,
                                                                         const jfloat A2x,
                                                                         const jfloat A2y,
                                                                         const jfloat B2x,
                                                                         const jfloat B2y,
                                                                         const jfloat C2x,
                                                                         const jfloat C2y,
                                                                         const jfloat D2x,
                                                                         const jfloat D2y,
                                                                         const jfloat A3x,
                                                                         const jfloat A3y,
                                                                         const jfloat B3x,
                                                                         const jfloat B3y,
                                                                         const jfloat C3x,
                                                                         const jfloat C3y,
                                                                         const jfloat D3x,
                                                                         const jfloat D3y) {

    jbyte *Buf1 = env->GetByteArrayElements(pSrcData1, 0);
    jbyte *Buf2 = env->GetByteArrayElements(pSrcData2, 0);
    jbyte *Buf3 = env->GetByteArrayElements(pSrcData3, 0);

    jbyte *BufOut = env->GetByteArrayElements(pOutData, 0);

    int original_point, pixel_point, X1, Y1;

    quat shape1 = {{{A1x, A1y}, {B1x, B1y}, {C1x, C1y}, {D1x, D1y}}};
    quat shape2 = {{{A2x, A2y}, {B2x, B2y}, {C2x, C2y}, {D2x, D2y}}};
    quat shape3 = {{{A3x, A3y}, {B3x, B3y}, {C3x, C3y}, {D3x, D3y}}};

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            pixel_point = (width * y + x) * 4;
            pt tests;
            tests.x = x;
            tests.y = y;
            if (inquat(shape1, tests)) {
                width00 = whSize0[index1][0];
                height00 = whSize0[index1][1];
                vec2 p(x, y);
                vec2 b(A1x, A1y);
                vec2 a(B1x, B1y);
                vec2 d(C1x, C1y);
                vec2 c(D1x, D1y);

                vec2 v2 = invBilinear(p, a, b, c, d);
                X1 = (int) ((float) width00 * v2.x);
                Y1 = (int) ((float) height00 * v2.y);

                original_point = (Y1 * width00 + X1) * 4;
                *(BufOut + pixel_point) = *(Buf1 + original_point);
                *(BufOut + pixel_point + 1) = *(Buf1 + original_point + 1);
                *(BufOut + pixel_point + 2) = *(Buf1 + original_point + 2);
//                *(BufOut + pixel_point) = 255;
//                *(BufOut + pixel_point + 1) = 0;
//                *(BufOut + pixel_point + 2) = 0;
            } else if (count > 1 && inquat(shape2, tests)) {
                width00 = whSize0[index2][0];
                height00 = whSize0[index2][1];
                vec2 p(x, y);
                vec2 a(A2x, A2y);
                vec2 d(B2x, B2y);
                vec2 c(C2x, C2y);
                vec2 b(D2x, D2y);

                vec2 v2 = invBilinear(p, a, b, c, d);
                X1 = (int) ((float) width00 * v2.x);
                Y1 = (int) ((float) height00 * v2.y);

                original_point = (Y1 * width00 + X1) * 4;
                *(BufOut + pixel_point) = *(Buf2 + original_point);
                *(BufOut + pixel_point + 1) = *(Buf2 + original_point + 1);
                *(BufOut + pixel_point + 2) = *(Buf2 + original_point + 2);
//                *(BufOut + pixel_point) = 0;
//                *(BufOut + pixel_point + 1) = 255;
//                *(BufOut + pixel_point + 2) = 0;
            } else if (count > 2 && inquat(shape3, tests)) {
                width00 = whSize0[index3][0];
                height00 = whSize0[index3][1];
                vec2 p(x, y);
                vec2 d(A3x, A3y);
                vec2 c(B3x, B3y);
                vec2 b(C3x, C3y);
                vec2 a(D3x, D3y);

                vec2 v2 = invBilinear(p, a, b, c, d);
                X1 = (int) ((float) width00 * v2.x);
                Y1 = (int) ((float) height00 * v2.y);

                original_point = (Y1 * width00 + X1) * 4;
                *(BufOut + pixel_point) = *(Buf3 + original_point);
                *(BufOut + pixel_point + 1) = *(Buf3 + original_point + 1);
                *(BufOut + pixel_point + 2) = *(Buf3 + original_point + 2);
//                *(BufOut + pixel_point) = 0;
//                *(BufOut + pixel_point + 1) = 0;
//                *(BufOut + pixel_point + 2) = 255;
            } else {
                *(BufOut + pixel_point) = 0;
                *(BufOut + pixel_point + 1) = 0;
                *(BufOut + pixel_point + 2) = 0;
            }
        }
    }

    env->ReleaseByteArrayElements(pSrcData1, Buf1, 0);
    env->ReleaseByteArrayElements(pSrcData2, Buf2, 0);
    env->ReleaseByteArrayElements(pSrcData3, Buf3, 0);

    env->ReleaseByteArrayElements(pOutData, BufOut, 0);
}

extern "C"
JNIEXPORT void JNICALL Java_com_frank_solid_PictureRingActivity_Initialization(JNIEnv *env, jclass obj,
                                                                               const jfloat Z,
                                                                               const jfloat cx,
                                                                               const jfloat cy,
                                                                               const jfloat cz) {
    DepthZ = Z;
    CenterX = cx;
    CenterY = cy;
    CenterZ = cz;
    Center.set(CenterX, CenterY, CenterZ);
}

extern "C"
JNIEXPORT void JNICALL Java_com_frank_solid_PictureRingActivity_Initialization2(JNIEnv *env, jclass obj,
                                                                                const jint width0,
                                                                                const jint height0,
                                                                                const jint width1,
                                                                                const jint height1,
                                                                                const jfloat R0,
                                                                                const jfloat r0,
                                                                                const jfloat rr0) {
    HPI = (double) height0 / 3.14159f;
    W0 = width0;
    W2PI = (double) width0 / 3.14159f / 2.0f;
    R = R0, r = r0, rr = rr0, FirstR = r;
    width = width1;
    height = height1;
 }

extern "C"
JNIEXPORT jint JNICALL Java_com_frank_solid_PictureRingActivity_transforms(JNIEnv *env, jclass obj,
                                                                           const jbyteArray pSrcData,
                                                                           const jbyteArray pOutData,
                                                                           const jfloat e_x,
                                                                           const jfloat e_y,
                                                                           const jfloat e_z,
                                                                           const jfloat r10) {
    eye_x = e_x;
    eye_y = e_y;
    eye_z = e_z;
    O.set(eye_x, eye_y, eye_z);

    r = r10;

    jbyte *Buf = env->GetByteArrayElements(pSrcData, 0);
    jbyte *BufOut = env->GetByteArrayElements(pOutData, 0);
    jint bmpLength = env->GetArrayLength(pSrcData);
    jint newBmpLength = env->GetArrayLength(pOutData);

    double latitude, longitude;
    int original_point, pixel_point, x0, y0;

    f4p = eye_z - DepthZ;
    f4c = eye_x * eye_x + eye_y * eye_y + eye_z * eye_z - R * R - rr * rr;
    f4e = 4 * R * R * (eye_z * eye_z - rr * rr) + f4c * f4c;
    for (int y = -height / 2; y < height / 2; y++) {
        if (y < - r || y > r) {
            for (int x = -width / 2; x < width / 2; x++) {
                pixel_point = (width * (y + height / 2) + x + width / 2) * 4;
                *(BufOut + pixel_point) = 0;
                *(BufOut + pixel_point + 1) = 0;
                *(BufOut + pixel_point + 2) = 0;
            }
            continue;
        }
        f4n = eye_y - y;
        for (int x = -width / 2; x < width / 2; x++) {
            pixel_point = (width * (y + height / 2) + x + width / 2) * 4;
            //if (getDistance2(x, y, 0, 0) < r)
            if (true)
            {

                f4m = eye_x - x;
                f4a = f4m * f4m + f4n * f4n  + f4p * f4p;
                f4a2 = f4a * f4a;
                f4b = 2 * eye_x * f4m + 2 * eye_y * f4n + 2 * eye_y * f4p;
                f4b2 = 2 * f4a * f4b;
                f4c2 = 2 * f4a * f4c + f4b * f4b + 4 * R * R * f4p * f4p;
                f4d = 2 * f4b * f4c + 8 * R * R * eye_z * f4p;

                m_z[0] = f4e;
                m_z[2] = f4d;
                m_z[4] = f4c2;
                m_z[6] = f4b2;
                m_z[8] = f4a2;
                if (x == 370 && y == 0) {
                    int aa = 0;
                }
                //求解方程
                int SolveCount = SolveEquation(m_z,m_x);
                float t = 1234567.89f;
                if (SolveCount == 4) {
                    if (abs(m_x[1]) < EPSILON) {
                        t = m_x[0];
                    } else if (abs(m_x[4]) < EPSILON){
                        t = m_x[3];
                    } else if (abs(m_x[7]) < EPSILON){
                        t = m_x[6];
                    } else if (abs(m_x[10]) < EPSILON){
                        t = m_x[9];
                    }
                }

                if (abs(t - 1234567.89f) < EPSILON) {
                    fu = -1;
                    fv = -1;
                } else {
                    fv = (eye_z + t * f4p) / rr;
                    if (fv < -1 || fv > 1) {
                        fu = -1;
                        fv = -1;
                    } else {
                        fu = (eye_x + f4m * t) / (R + rr * cos(fv));
                        if (fu < -1 || fu > 1) {
                            fu = -1;
                            fv = -1;
                        } else {
                            fu = acos(fu);
                            fv = asin(fv);
                        }
                    }
                }
                if (fu < 0 || fv < 0 || fu > 2 * 3.14159f || fv > 2 * 3.14159f ) {
                    *(BufOut + pixel_point) = 0;
                    *(BufOut + pixel_point + 1) = 0;
                    *(BufOut + pixel_point + 2) = 0;
                } else {
                    original_point = ((int) (HPI / 2 * fu) * W0 + (int) (W2PI * fv)) * 4;
                    *(BufOut + pixel_point) = *(Buf + original_point);
                    *(BufOut + pixel_point + 1) = *(Buf + original_point + 1);
                    *(BufOut + pixel_point + 2) = *(Buf + original_point + 2);
                }
            } else {
                *(BufOut + pixel_point) = 0;
                *(BufOut + pixel_point + 1) = 0;
                *(BufOut + pixel_point + 2) = 0;
            }
        }
    }
    env->ReleaseByteArrayElements(pSrcData, Buf, 0);
    env->ReleaseByteArrayElements(pOutData, BufOut, 0);

    return 0;
}



