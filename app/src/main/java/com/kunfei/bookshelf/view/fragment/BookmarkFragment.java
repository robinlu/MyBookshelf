package com.kunfei.bookshelf.view.fragment;

import android.os.Bundle;

import com.hwangjr.rxbus.RxBus;
import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseFragment;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookmarkBean;
import com.kunfei.bookshelf.bean.OpenChapterBean;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.RxBusTag;
import com.kunfei.bookshelf.view.activity.ChapterListActivity;
import com.kunfei.bookshelf.view.adapter.ChapterListAdapter;
import com.kunfei.bookshelf.widget.modialog.EditBookmarkView;
import com.kunfei.bookshelf.widget.modialog.MoDialogHUD;
import com.kunfei.bookshelf.widget.recycler.scroller.FastScrollRecyclerView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BookmarkFragment extends MBaseFragment {
    @BindView(R.id.rv_list)
    FastScrollRecyclerView rvList;

    private Unbinder unbinder;
    public MoDialogHUD moDialogHUD;
    private BookShelfBean bookShelf;
    private ChapterListAdapter chapterListAdapter;

    @Override
    public int createLayoutId() {
        return R.layout.fragment_bookmark_list;
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        super.initData();
        moDialogHUD = new MoDialogHUD(getActivity());
        bookShelf = ((ChapterListActivity) getActivity()).getBookShelf();
    }

    /**
     * 控件绑定
     */
    @Override
    protected void bindView() {
        super.bindView();
        unbinder = ButterKnife.bind(this, view);
        chapterListAdapter = new ChapterListAdapter(bookShelf, new ChapterListAdapter.OnItemClickListener() {
            @Override
            public void itemClick(int index, int page, int tabPosition) {
                ((ChapterListActivity) getActivity()).searchViewCollapsed();
                if (index != bookShelf.getDurChapter()) {
                    RxBus.get().post(RxBusTag.SKIP_TO_CHAPTER, new OpenChapterBean(index, page));
                }
                getActivity().finish();
            }

            @Override
            public void itemLongClick(BookmarkBean bookmarkBean, int tabPosition) {
                ((ChapterListActivity) getActivity()).searchViewCollapsed();
                showBookmark(bookmarkBean);
            }
        });
        rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvList.setAdapter(chapterListAdapter);
        chapterListAdapter.tabChange(1);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        RxBus.get().unregister(this);
    }

    public void startSearch(String key) {
        chapterListAdapter.search(key);
    }

    private void showBookmark(BookmarkBean bookmarkBean) {
        moDialogHUD.showBookmark(bookmarkBean, false, new EditBookmarkView.OnBookmarkClick() {
            @Override
            public void saveBookmark(BookmarkBean bookmarkBean) {
                DbHelper.getInstance().getmDaoSession().getBookmarkBeanDao().insertOrReplace(bookmarkBean);
                bookShelf.getBookInfoBean().setBookmarkList(BookshelfHelp.getBookmarkList(bookShelf.getBookInfoBean().getName()));
                chapterListAdapter.notifyDataSetChanged();
            }

            @Override
            public void delBookmark(BookmarkBean bookmarkBean) {
                DbHelper.getInstance().getmDaoSession().getBookmarkBeanDao().delete(bookmarkBean);
                bookShelf.getBookInfoBean().setBookmarkList(BookshelfHelp.getBookmarkList(bookShelf.getBookInfoBean().getName()));
                chapterListAdapter.notifyDataSetChanged();
            }

            @Override
            public void openChapter(int chapterIndex, int pageIndex) {
                RxBus.get().post(RxBusTag.OPEN_BOOK_MARK, bookmarkBean);
                getActivity().finish();
            }
        });

    }

}