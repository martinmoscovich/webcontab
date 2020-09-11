import { VuexModule, Module, Mutation, RegisterOptions } from 'vuex-class-modules';

@Module
export class UIStore extends VuexModule {
  /* NavBar */
  isNavBarVisible = true;

  /* FooterBar */
  isFooterBarVisible = true;

  /* Aside */
  isAsideVisible = true;
  isAsideMobileExpanded = false;
  reducedFlag = false;

  /** Ancho de la app */
  private width = 0;

  constructor(options: RegisterOptions) {
    super(options);
    this.onResize();
  }

  /** Indica si es ancho de mobile */
  get isMobile() {
    return this.width < 768;
  }

  /** Indica si es ancho de tablet */
  get isTablet() {
    return !this.isMobile && this.width < 1024;
  }

  /** Indica si es ancho de desktop */
  get isDesktop() {
    return this.width >= 1024;
  }

  get isAsideReduced() {
    return this.reducedFlag && !this.isMobile;
  }

  @Mutation
  onResize() {
    this.width = window.innerWidth;
  }

  @Mutation
  asideReducedToggle(value?: boolean) {
    if (value !== undefined) {
      this.reducedFlag = value;
    } else {
      this.reducedFlag = !this.reducedFlag;
    }
  }

  /* Aside Mobile */
  @Mutation
  asideMobileStateToggle(value?: boolean) {
    const htmlClassName = 'has-aside-mobile-expanded';

    let isShow: boolean;

    if (value !== undefined) {
      isShow = value;
    } else {
      isShow = !this.isAsideMobileExpanded;
    }

    if (isShow) {
      document.documentElement.classList.add(htmlClassName);
    } else {
      document.documentElement.classList.remove(htmlClassName);
    }

    this.isAsideMobileExpanded = isShow;
  }
}
