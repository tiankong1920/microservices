type NavigateFn = (path: string) => void;

let navigateFn: NavigateFn | null = null;

export const setNavigate = (fn: NavigateFn): void => {
  navigateFn = fn;
};

export const navigate = (path: string): void => {
  if (navigateFn) {
    navigateFn(path);
  } else {
    window.location.href = path;
  }
};
