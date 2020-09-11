import { Location } from 'vue-router';

export interface MenuItem extends MenuParent {
  name: string;
  label: string;
  subLabel?: string;
  icon?: string;
  to?: string | Location;
  href?: string;
  onlyEdition?: boolean;
  roles?: string[];
}

export interface MenuGroup extends MenuParent {
  name: string;
}

export interface MenuParent {
  items?: MenuItem[];
}
