import { createRouter, createWebHistory } from 'vue-router'
import { clearUser, getUser, hasRole, isLogin } from '../utils/auth'

const Login = () => import('../views/Login.vue')
const Layout = () => import('../layout/Layout.vue')
const Home = () => import('../views/Home.vue')
const Register = () => import('../views/Register.vue')
const UserManage = () => import('../views/UserManage.vue')
const ArtisanManage = () => import('../views/ArtisanManage.vue')
const StudentManage = () => import('../views/StudentManage.vue')
const CraftManage = () => import('../views/CraftManage.vue')
const TraceManage = () => import('../views/TraceManage.vue')
const TraceQuery = () => import('../views/TraceQuery.vue')
const TraceAdd = () => import('../views/TraceAdd.vue')
const PublicTraceQuery = () => import('../views/PublicTraceQuery.vue')
const ImageSimilaritySearch = () => import('../views/ImageSimilaritySearch.vue')
const CourseList = () => import('../views/CourseList.vue')
const CourseDetail = () => import('../views/CourseDetail.vue')
const MyCourses = () => import('../views/MyCourses.vue')
const CourseManage = () => import('../views/CourseManage.vue')
const WorkshopManage = () => import('../views/WorkshopManage.vue')
const CourseAttendanceList = () => import('../views/CourseAttendanceList.vue')
const CourseReviewList = () => import('../views/CourseReviewList.vue')
const CourseBookingList = () => import('../views/CourseBookingList.vue')
const MaterialManage = () => import('../views/MaterialManage.vue')
const Profile = () => import('../views/Profile.vue')
const MyReviews = () => import('../views/MyReviews.vue')
const ReviewManage = () => import('../views/ReviewManage.vue')
const TeacherManage = () => import('../views/TeacherManage.vue')
const StudentShowcase = () => import('../views/StudentShowcase.vue')

const routes = [
  { path: '/', redirect: () => (isLogin() ? '/app/home' : '/login') },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/trace', name: 'PublicTraceQuery', component: PublicTraceQuery },
  { path: '/image-search', name: 'ImageSimilaritySearch', component: ImageSimilaritySearch },
  { path: '/public/trace', redirect: (to) => ({ path: '/trace', query: to.query }) },
  { path: '/public/image-search', redirect: (to) => ({ path: '/image-search', query: to.query }) },
  {
    path: '/app',
    component: Layout,
    redirect: '/app/home',
    children: [
      { path: 'home', name: 'Home', component: Home },
      { path: 'profile', name: 'Profile', component: Profile },
      { path: 'my/reviews', name: 'MyReviews', component: MyReviews },
      { path: 'my/works', name: 'StudentShowcase', component: StudentShowcase, meta: { studentOnly: true } },
      {
        path: 'review/manage',
        name: 'ReviewManage',
        component: ReviewManage,
        meta: { handicraftOnly: true }
      },
      { path: 'teachers', name: 'TeacherManage', component: TeacherManage, meta: { adminOrHandicraft: true } },
      { path: 'users', name: 'UserManage', component: UserManage, meta: { adminOrHandicraft: true } },
      { path: 'artisans', name: 'ArtisanManage', component: ArtisanManage, meta: { adminStrict: true } },
      { path: 'students', name: 'StudentManage', component: StudentManage, meta: { adminStrict: true } },
      { path: 'materials', name: 'MaterialManage', component: MaterialManage, meta: { handicraftOnly: true } },
      { path: 'crafts', name: 'CraftManage', component: CraftManage, meta: { adminOrHandicraft: true } },
      { path: 'traces', name: 'TraceManage', component: TraceManage, meta: { handicraftOnly: true } },
      { path: 'query', name: 'TraceQuery', component: TraceQuery },
      { path: 'trace/add', name: 'TraceAdd', component: TraceAdd, meta: { handicraftOnly: true } },
      { path: 'courses', name: 'CourseList', component: CourseList },
      { path: 'course/:id', name: 'CourseDetail', component: CourseDetail },
      { path: 'my/courses', name: 'MyCourses', component: MyCourses },
      { path: 'course/manage', name: 'CourseManage', component: CourseManage, meta: { handicraftOnly: true } },
      { path: 'workshop/manage', name: 'WorkshopManage', component: WorkshopManage, meta: { handicraftOnly: true } },
      { path: 'course/:id/attendance', name: 'CourseAttendanceList', component: CourseAttendanceList, meta: { handicraftOnly: true } },
      { path: 'course/:id/bookings', name: 'CourseBookingList', component: CourseBookingList, meta: { handicraftOnly: true } },
      { path: 'course/:id/reviews', name: 'CourseReviewList', component: CourseReviewList, meta: { handicraftOnly: true } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: () => (isLogin() ? '/app/home' : '/login') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (
    to.path === '/trace' ||
    to.path === '/image-search' ||
    to.path === '/public/trace' ||
    to.path === '/public/image-search' ||
    to.path === '/register'
  ) {
    return next()
  }
  if (to.path === '/login') return isLogin() ? next('/app/home') : next()

  if (!to.path.startsWith('/app')) return next()
  if (!isLogin()) return next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)

  const user = getUser()
  if (!user?.role) {
    clearUser()
    return next('/login')
  }

  if (to.meta?.adminStrict && !hasRole('admin')) return next('/app/home')
  if (to.meta?.studentOnly && !hasRole('student')) return next('/app/home')
  if (to.meta?.handicraftOnly && !hasRole('handicraft', 'admin')) return next('/app/home')
  if (to.meta?.adminOrHandicraft && !hasRole('admin', 'handicraft')) return next('/app/home')

  next()
})

export default router
