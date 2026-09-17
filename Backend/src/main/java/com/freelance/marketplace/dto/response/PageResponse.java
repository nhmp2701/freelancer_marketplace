package com.freelance.marketplace.dto.response;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Builder
public class PageResponse<T> {
  private List<T> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
  private boolean last;

  public static <E, D> PageResponse<D> from(Page<E> page, Function<E, D> mapper) {
    return PageResponse.<D>builder()
        .content(page.getContent().stream().map(mapper).collect(Collectors.toList()))
        .page(page.getNumber())
        .size(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .last(page.isLast())
        .build();
  }
}
