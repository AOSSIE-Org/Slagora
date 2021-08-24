import { NbMenuItem } from '@nebular/theme';

export const NORMAL_MENU_ITEMS: NbMenuItem[] = [
  {
    title: 'Dashboard',
    icon: 'home-outline',
    link: '/pages/dashboard',
    home: true,
  },
  {
    title: 'New Shipment',
    icon: 'edit-2-outline',
    link: '/pages/new'
  },
  {
    title: 'Authentication',
    group: true,
  },
  {
    title: 'Change your password',
    icon: 'person-outline',
    link: '/auth/change-password'
  },
  {
    title: 'Logout',
    icon: 'power-outline',
  }
];

export const ADMIN_MENU_ITEMS: NbMenuItem[] = [
  {
    title: 'Dashboard',
    icon: 'home-outline',
    link: '/pages/dashboard',
    home: true,
  },
  {
    title: 'New Shipment',
    icon: 'edit-2-outline',
    link: '/pages/new'
  },
  {
    title: 'Authentication',
    group: true,
  },
  {
    title: 'Change your password',
    icon: 'person-outline',
    link: '/auth/change-password'
  },
  {
    title: 'Change user password',
    icon: 'lock-outline',
    link: '/auth/change-user-password',
  },
  {
    title: 'Register new user',
    icon: 'person-add-outline',
    link: '/auth/signup',
  },
  {
    title: 'Logout',
    icon: 'power-outline',
  }
];
