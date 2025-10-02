package newblogproject.example.newproject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SliceResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private boolean hasNext;

    public SliceResponse(List<T> content, int pageNumber, int pageSize, boolean hasNext) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.hasNext = hasNext;
    }


}
